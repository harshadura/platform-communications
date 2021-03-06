package org.motechproject.callflow.service;

import org.apache.commons.lang.StringEscapeUtils;
import org.motechproject.callflow.domain.FlowSessionRecord;
import org.motechproject.ivr.domain.CallEventLog;
import org.motechproject.callflow.domain.IvrEvent;
import org.motechproject.decisiontree.core.DecisionTreeService;
import org.motechproject.decisiontree.core.model.FlowSession;
import org.motechproject.decisiontree.core.TreeNodeLocator;
import org.motechproject.decisiontree.core.model.CallStatus;
import org.motechproject.decisiontree.core.model.DialStatus;
import org.motechproject.decisiontree.core.model.INodeOperation;
import org.motechproject.decisiontree.core.model.ITransition;
import org.motechproject.decisiontree.core.model.Node;
import org.motechproject.decisiontree.core.model.Transition;
import org.motechproject.event.listener.EventRelay;
import org.motechproject.ivr.domain.CallDetailRecord;
import org.motechproject.ivr.domain.CallEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.motechproject.commons.date.util.DateUtil.now;

@Service
public class CallFlowServerImpl implements CallFlowServer {

    private Logger logger = LoggerFactory.getLogger((this.getClass()));

    public static final String TEMPLATE_BASE_PATH = "/vm/";
    public static final String NODE_TEMPLATE_NAME = TEMPLATE_BASE_PATH + "node";
    public static final String ERROR_MESSAGE_TEMPLATE_NAME = TEMPLATE_BASE_PATH + "error";
    public static final String EXIT_TEMPLATE_NAME = TEMPLATE_BASE_PATH + "exit";
    private static final String FLOW_SESSION_ID_FIELD = "flowSessionId";

    public static final Integer MAX_INPUT_DIGITS = 50;
    public static final Integer MAX_INPUT_TIMEOUT = 5000;
    public static final String DEFAULT_END_OF_KEY_MARKER = "#";

    private DecisionTreeService decisionTreeService;
    private TreeEventProcessor treeEventProcessor;
    private ApplicationContext applicationContext;
    private FlowSessionService flowSessionService;
    private EventRelay eventRelay;

    @Autowired
    public CallFlowServerImpl(DecisionTreeService decisionTreeService, TreeEventProcessor treeEventProcessor, ApplicationContext applicationContext, FlowSessionService flowSessionService, EventRelay eventRelay) {
        this.decisionTreeService = decisionTreeService;
        this.treeEventProcessor = treeEventProcessor;
        this.applicationContext = applicationContext;
        this.flowSessionService = flowSessionService;
        this.eventRelay = eventRelay;
    }

    @Override
    public ModelAndView getResponse(String flowSessionId, String phoneNumber, String provider, String tree, String transitionKey, String language) {

        FlowSession session = flowSessionService.findOrCreate(flowSessionId, phoneNumber);
        if (language != null) {
            session.setLanguage(language);
        }

        ModelAndView view;
        try {
            view = getModelViewForNextNode(session, provider, tree, transitionKey);
        } catch (DecisionTreeException e) {
            logger.error(e.getMessage(), e);
            view = getErrorModelAndView(e.subject, session, provider);
        } catch (Exception e) {
            logger.error(format("Unexpected exception %s", e.getMessage()), e);
            view = getErrorModelAndView(Error.UNEXPECTED_EXCEPTION, session, provider);
        }
        return view;
    }

    @Override
    public void raiseCallEvent(IvrEvent callEvent, String flowSessionId) {
        FlowSession session = flowSessionService.getSession(flowSessionId);
        CallDetailRecord callDetail = session.getCallDetailRecord();
        if (callEvent.isEndOfCall()) {
            callDetail.setEndDate(now()).addCallEvent(new CallEventLog(callEvent.getEventSubject()));
            flowSessionService.updateSession(session);
        }
        eventRelay.sendEventMessage(new CallEvent(callEvent.getEventSubject(), callDetail).toMotechEvent());
    }

    private ModelAndView getModelViewForNextNode(FlowSession session, String provider, String tree, String transitionKey) {

        if (CallStatus.Hangup.toString().equals(transitionKey) || CallStatus.Disconnect.toString().equals(transitionKey)) {
            return new ModelAndView(templateNameFor(provider, EXIT_TEMPLATE_NAME));
        }

        if (isBlank(session.getLanguage()) || isBlank(tree)) {
            logger.error(format("No tree or language specified"));
            return getErrorModelAndView(Error.TREE_OR_LANGUAGE_MISSING, session, provider);
        }

        Node node = getCurrentNode(session);
        try {
            if (node == null) {
                addFlowStartEvent(session, tree);
                node = decisionTreeService.getRootNode(tree, session);
                autowire(node);
                executeOperations(transitionKey, session, node);
            } else {
                addDTMFEvent(session, transitionKey);
            }

            validateNode(node);

            if (transitionKey == null) {
                return constructModelViewForNode(node, session, provider, tree);
            } else {
                ITransition nextTransition = getTransitionForUserInput(transitionKey, node);
                autowire(nextTransition);

                Map<String, Object> params = new HashMap<String, Object>();
                params.put(FLOW_SESSION_ID_FIELD, session.getSessionId());

                treeEventProcessor.sendActionsAfter(node, params);

                if (nextTransition instanceof Transition) {
                    treeEventProcessor.sendTransitionActions((Transition) nextTransition, params);
                }

                node = nextTransition.getDestinationNode(transitionKey, session);
                autowire(node);

                if (isEmptyNode(node)) {
                    return new ModelAndView(templateNameFor(provider, EXIT_TEMPLATE_NAME));
                } else {
                    executeOperations(transitionKey, session, node);
                    return constructModelViewForNode(node, session, provider, tree);
                }
            }
        } finally {
            flowSessionService.updateSession(session);
        }
    }

    private void addDTMFEvent(FlowSession session, String transitionKey) {
        final CallEventLog callEventLog = new CallEventLog(IvrEvent.Dtmf.getEventSubject());
        callEventLog.appendData("input", transitionKey);

        saveEvent(session, callEventLog);
    }

    private void addFlowStartEvent(FlowSession session, String tree) {
        final CallEventLog callEventLog = new CallEventLog("FlowStart:" + tree);
        callEventLog.appendData("treeName", tree);

        saveEvent(session, callEventLog);
    }

    private void saveEvent(FlowSession session, CallEventLog callEventLog) {
        CallDetailRecord callDetailRecord = ((FlowSessionRecord) session).getCallDetailRecord();
        callDetailRecord.addCallEvent(callEventLog);
        flowSessionService.updateSession(session);
    }

    private ModelAndView constructModelViewForNode(Node node, FlowSession session, String provider, String tree) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(FLOW_SESSION_ID_FIELD, session.getSessionId());

        treeEventProcessor.sendActionsBefore(node, params);

        ModelAndView mav = new ModelAndView();
        mav.setViewName(templateNameFor(provider, NODE_TEMPLATE_NAME));
        mav.addObject("treeName", tree);
        mav.addObject("node", node);
        mav.addObject("provider", provider);
        mav.addObject("language", session.getLanguage());
        mav.addObject("escape", new StringEscapeUtils());
        mav.addObject("maxDigits", maxDigits(node));
        mav.addObject("maxTimeout", maxTimeout(node));
        mav.addObject("transitionKeyEndMarker", transitionKeyEndMarker(node));
        mav.addObject("isUserInputNeeded", isUserInputNeeded(node));
        session.setCurrentNode(node);
        return mav;
    }

    private boolean isEmptyNode(Node node) {
        return node == null || hasNoActionableItems(node);
    }

    private boolean hasNoActionableItems(Node node) {
        return node.getPrompts().isEmpty() && hasNoActions(node) && node.getTransitions().isEmpty();
    }

    private boolean hasNoActions(Node node) {
        return node.getActionsAfter().isEmpty() && node.getActionsBefore().isEmpty();
    }

    private void executeOperations(String transitionKey, FlowSession session, Node node) {
        for (INodeOperation operation : node.getOperations()) {
            operation.perform(transitionKey, session);
        }
    }

    private void autowire(Object transition) {
        applicationContext.getAutowireCapableBeanFactory().autowireBean(transition);
    }

    private Node getCurrentNode(FlowSession session) {
        return session.getCurrentNode();
    }

    private void validateNode(Node node) {
        for (Map.Entry<String, ITransition> transitionEntry : node.getTransitions().entrySet()) {
            final String key = transitionEntry.getKey();

            if (noInput(key) || hasSpecialMeaning(key) || dtmfKey(key)) {
                return;
            }

            if (anyInput(key)) {
                return;
            }

            if (isStatusKey(key)) {
                return;
            }

            try {
                Integer.parseInt(key);
            } catch (NumberFormatException e) {
                throw new DecisionTreeException(Error.INVALID_TRANSITION_KEY, format("Invalid transition key [%s] for node [%s]", key, node), e);
            }

            ITransition transition = transitionEntry.getValue();
            if (transition instanceof Transition && ((Transition) transition).getDestinationNode() == null) {
                throw new DecisionTreeException(Error.NULL_DESTINATION_NODE, format("Missing destination node in the transition for key [%s] on node [%s]: ", key, node));
            }
        }
    }

    private boolean isStatusKey(String key) {
        return DialStatus.isValid(key) || CallStatus.isValid(key);
    }

    private boolean hasSpecialMeaning(String key) {
        return (anyInput(key) || DialStatus.isValid(key));
    }

    private boolean noInput(String key) {
        return TreeNodeLocator.NO_INPUT.equals(key);
    }

    private boolean dtmfKey(String key) {
        return "*#".contains(key);
    }

    private boolean anyInput(String key) {
        return TreeNodeLocator.ANY_KEY.equals(key);
    }

    private ITransition getTransitionForUserInput(String userInput, Node parentNode) {
        ITransition transition = getPreConfiguredTransition(parentNode, userInput);
        if (transition == null) {
            transition = parentNode.getTransitions().get(TreeNodeLocator.ANY_KEY);
        }
        if (transition == null) {
            throw new DecisionTreeException(Error.INVALID_TRANSITION_KEY, "Invalid Transition Key. There is no transition with key: " + userInput + " in the Node: " + parentNode);
        }
        return transition;
    }

    private ITransition getPreConfiguredTransition(Node parentNode, String userInput) {
        return parentNode.getTransitions().get(userInput);
    }

    private Integer maxDigits(Node node) {
        Map<String, ITransition> transitions = node.getTransitions();
        int maxDigits = 1;
        for (String key : transitions.keySet()) {
            if (hasSpecialMeaning(key)) {
                return (node.getMaxTransitionInputDigit() == null) ? MAX_INPUT_DIGITS : node.getMaxTransitionInputDigit();
            }
            if (maxDigits < key.length()) {
                maxDigits = key.length();
            }
        }
        return maxDigits;
    }

    private Integer maxTimeout(Node node) {
        return node.getMaxTransitionTimeout() == null ? MAX_INPUT_TIMEOUT : node.getMaxTransitionTimeout();
    }

    private boolean isUserInputNeeded(Node node) {
        return node.hasTransitions() && !node.hasDialPrompts();
    }

    private String transitionKeyEndMarker(Node node) {
        return node.getTransitionKeyEndMarker() == null ? DEFAULT_END_OF_KEY_MARKER : node.getTransitionKeyEndMarker();
    }

    private String templateNameFor(String type, String templateName) {
        return templateName + "-" + type;
    }

    private ModelAndView getErrorModelAndView(Error error, FlowSession session, String provider) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName(templateNameFor(provider, ERROR_MESSAGE_TEMPLATE_NAME));
        mav.addObject("message", error.toString());
        mav.addObject("language", session.getLanguage());
        return mav;
    }

    private class DecisionTreeException extends RuntimeException {

        private Error subject;

        public DecisionTreeException(Error subject, String description) {
            super(description);
            this.subject = subject;
        }

        public DecisionTreeException(Error subject, String description, Throwable cause) {
            super(description, cause);
            this.subject = subject;
        }
    }
}
