(function () {
    'use strict';

    /* Directives */

    var widgetModule = angular.module('motech-sms');

   widgetModule.directive('jqgridSearch', function () {
           return {
               restrict: 'A',
               link: function (scope, element, attrs) {
                   var elem = angular.element(element),
                       table = angular.element('#' + attrs.jqgridSearch),
                       eventType = elem.data('event-type'),
                       filter = function (time) {
                           var field = elem.data('search-field'),
                               value = elem.data('search-value'),
                               type = elem.data('field-type') || 'string',
                               url = parseUri(table.jqGrid('getGridParam', 'url')),
                               query = {},
                               params = '?',
                               array = [],
                               prop;

                           // copy existing url parameters
                           for (prop in url.queryKey) {
                               if (prop !== field) {
                                   query[prop] = url.queryKey[prop];
                               }
                           }

                           // set parameter for given element
                           switch (type) {
                           case 'boolean':
                               query[field] = url.queryKey[field].toLowerCase() !== 'true';

                               if (query[field]) {
                                   elem.find('i').removeClass('icon-ban-circle').addClass('icon-ok');
                               } else {
                                   elem.find('i').removeClass('icon-ok').addClass('icon-ban-circle');
                               }
                               break;
                           case 'array':
                               angular.forEach(url.queryKey[field].split(','), function (val) {
                                   if (value === val) {
                                       if (elem.children().hasClass("icon-ok")) {
                                           elem.children().removeClass("icon-ok").addClass("icon-ban-circle");
                                       } else {
                                           elem.children().removeClass("icon-ban-circle").addClass("icon-ok");
                                       }
                                   }
                                   if (angular.element('#' + val).children().hasClass("icon-ok")) {
                                       array.push(val);
                                   }
                               });

                               query[field] = array.join(',');
                               break;
                           default:
                               query[field] = elem.val();
                           }

                           // create raw parameters
                           for (prop in query) {
                               params += prop + '=' + query[prop] + '&';
                           }

                           // remove last '&'
                           params = params.slice(0, params.length - 1);

                           if (timeoutHnd) {
                               clearTimeout(timeoutHnd);
                           }

                           timeoutHnd = setTimeout(function () {
                               jQuery('#' + attrs.jqgridSearch).jqGrid('setGridParam', {
                                   url: '../smsapi/smslogging' + params
                               }).trigger('reloadGrid');
                           }, time || 0);
                       },
                       timeoutHnd;

                   switch (eventType) {
                   case 'keyup':
                       elem.keyup(function () {
                           filter(500);
                       });
                       break;
                   default:
                       elem.click(filter);
                   }
               }
           };
       });

    widgetModule.directive('loggingGrid', function ($compile) {
        return {
            restrict: 'A',
            link: function (scope, element, attrs) {
                var elem = angular.element(element);

                elem.jqGrid({
                    caption: 'SMS Logging',
                    url: '../smsapi/smslogging?phoneNumber=&messageContent=&timeFrom=&timeTo=&deliveryStatus=INPROGRESS,DELIVERED,KEEPTRYING,ABORTED,UNKNOWN&smsType=INBOUND,OUTBOUND',
                    datatype: 'json',
                    jsonReader:{
                        repeatitems:false
                    },
                    prmNames: {
                        sort: 'sortColumn',
                        order: 'sortDirection'
                    },
                    shrinkToFit: true,
                    autowidth: true,
                    rownumbers: true,
                    rowNum: 5,
                    rowList: [5, 20, 30],
                    colModel: [{
                        name: 'phoneNumber',
                        index: 'phoneNumber'
                    }, {
                        name: 'deliveryStatus',
                        index: 'deliveryStatus'
                    }, {
                        name: 'messageTime',
                        index: 'messageTime'
                    }, {
                        name: 'smsType',
                        index: 'smsType'
                    }, {
                        name: 'messageContent',
                        index: 'messageContent',
                        sortable: false
                    }],
                    pager: '#' + attrs.loggingGrid,
                    width: '100%',
                    height: 'auto',
                    sortname: 'phoneNumber',
                    sortorder: 'asc',
                    viewrecords: true,
                    toolbar: [true,'top'],
                    gridComplete: function () {
                        $('#outsideResourceTable').children('div').width('100%');
                        $('.ui-jqgrid-htable').addClass('table-lightblue');
                        $('.ui-jqgrid-bdiv').width('100%');
                        $('.ui-jqgrid-hdiv').width('100%');
                        $('.ui-jqgrid-hbox').width('100%');
                        $('.ui-jqgrid-view').width('100%');
                        $('#t_resourceTable').width('auto');
                        $('.ui-jqgrid-pager').width('100%');
                        $('.ui-jqgrid-pager').height('auto');
                        $('#outsideResourceTable').children('div').each(function() {
                            $('table', this).width('100%');
                            $(this).find('#resourceTable').width('100%');
                            $(this).find('table').width('100%');
                       });
                    }
                });

                $('#t_resourceTable').append($compile($('#operations-resource'))(scope));
                $('#t_resourceTable').append($compile($('#collapse-resource'))(scope));
            }
        };
    });
}());