(function () {
    'use strict';

    /* Directives */

    var widgetModule = angular.module('motech-cmslite');

    widgetModule.directive('innerlayout', function() {
        return {
            restrict: 'EA',
            link: function(scope, elm, attrs) {
                var eastSelector;
                /*
                * Define options for inner layout
                */
                scope.innerLayoutOptions = {
                    name: 'innerLayout',
                    resizable: true,
                    slidable: true,
                    closable: true,
                    east__paneSelector: "#inner-east",
                    center__paneSelector: "#inner-center",
                    east__spacing_open: 6,
                    spacing_closed: 30,
                    east__size: 300,
                    east__minSize: 200,
                    east__maxSize: 350,
                    showErrorMessages: true, // some panes do not have an inner layout
                    resizeWhileDragging: true,
                    center__minHeight: 100,
                    contentSelector: ".ui-layout-content",
                    togglerContent_open: '',
                    togglerContent_closed: '<div><i class="icon-caret-left button"></i></div>',
                    autoReopen: false, // auto-open panes that were previously auto-closed due to 'no room'
                    noRoom: true,
                    togglerAlign_closed: "top", // align to top of resizer
                    togglerAlign_open: "top",
                    togglerLength_open: 0,
                    togglerLength_closed: 35,
                    togglerTip_open: "Close This Pane",
                    togglerTip_closed: "Open This Pane",
                    east__initClosed: false,
                    initHidden: false
                };

                // create the page-layout, which will ALSO create the tabs-wrapper child-layout
                scope.innerLayout = elm.layout(scope.innerLayoutOptions);

                // BIND events to hard-coded buttons
                scope.innerLayout.addCloseBtn( "#tbarCloseEast", "east" );
                scope.innerLayout.addToggleBtn("#cmslite-resource-filters", "east");
            }
        };
    });

    widgetModule.directive('clearForm', function () {
        return {
            restrict: 'A',
            link: function (scope, element, attrs) {
                $(element).on('hidden', function () {
                    $('#' + attrs.clearForm).clearForm();
                });
            }
        };
    });

    widgetModule.directive('autoComplete', function () {
        return {
            restrict: 'A',
            link: function (scope, element, attrs) {
                angular.element(element).autocomplete({
                    minLength: 2,
                    source: function (request, response) {
                        $.getJSON('../cmsliteapi/resource/available/' + attrs.autoComplete, request, function (data) {
                            response(data);
                        });
                    }
                });
            }
        };
    });

    widgetModule.directive('jqgridSearch', function () {
        return {
            restrict: 'A',
            link: function (scope, element, attrs) {
                var elem = angular.element(element),
                    table = angular.element('#' + attrs.jqgridSearch),
                    eventType = elem.data('event-type'),
                    timeoutHnd,
                    filter = function (time) {
                        var field = elem.data('search-field'),
                            type = elem.data('field-type') || 'string',
                            url = parseUri(jQuery('#' + attrs.jqgridSearch).jqGrid('getGridParam', 'url')),
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
                            angular.forEach(url.queryKey[field].split(','), function (value) {
                                if (elem.not(':checked') && elem.val() !== value && value !== '') {
                                    array.push(value);
                                }
                            });

                            if (elem.is(':checked')) {
                                array.push(elem.val());
                            }

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
                                url: '../cmsliteapi/resource' + params
                            }).trigger('reloadGrid');
                        }, time || 0);
                    };

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

    widgetModule.directive('resourcesGrid', function ($compile) {
        return {
            restrict: 'A',
            link: function (scope, element, attrs) {
                var elem = angular.element(element);

                elem.jqGrid({
                    url: '../cmsliteapi/resource?name=&string=true&stream=true&languages=',
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
                    rowNum: 10,
                    rowList: [10, 20, 50],
                    colModel: [{
                        name: 'name',
                        index: 'name',
                        width: '200'
                    }, {
                        name: 'languages',
                        index: 'languages',
                        width: '200',
                        sortable: false,
                        formatter: function (array, options, data) {
                            var ul = $('<ul>');

                            angular.forEach(array, function (value) {
                                ul.append($('<li>').append($('<a>')
                                    .append(value)
                                    .attr('ng-click', 'showResource("{0}", "{1}", "{2}")'.format(data.type, value, data.name))
                                    .css('cursor', 'pointer')
                                ));
                            });

                            return '<ul>' + ul.html() + '</ul>';
                        }
                    }, {
                        name: 'type',
                        index: 'type',
                        width: '100',
                        align: 'center',
                        formatter: function (value) {
                            return scope.msg('cmslite.resource.type.' + value);
                        }
                    }],
                    pager: '#' + attrs.resourcesGrid,
                    width: '100%',
                    height: 'auto',
                    sortname: 'name',
                    sortorder: 'asc',
                    viewrecords: true,
                    gridComplete: function () {
                        $.ajax({
                            url: '../server/lang/locate',
                            success:  function() {},
                            async: false
                        });

                        angular.forEach(elem.find('ul'), function(value) {
                            $compile(value)(scope);
                        });

                        angular.forEach(['name', 'languages', 'type'], function (value) {
                            elem.jqGrid('setLabel', value, scope.msg('cmslite.resource.' + value));
                        });

                        $('#outsideResourceTable').children('div').width('100%');
                        $('.ui-jqgrid-htable').addClass("table-lightblue");
                        $('.ui-jqgrid-btable').addClass("table-lightblue");
                        $('.ui-jqgrid-htable').width('100%');
                        $('.ui-jqgrid-bdiv').width('100%');
                        $('.ui-jqgrid-hdiv').width('100%');
                        $('.ui-jqgrid-view').width('100%');
                        $('#t_resourceTable').width('auto');
                        $('.ui-jqgrid-pager').width('100%');
                        $('.ui-jqgrid-hbox').css({'padding-right':'0'});
                        $('.ui-jqgrid-hbox').width('100%');
                        $('#outsideResourceTable').children('div').each(function() {
                            $(this).find('table').width('100%');
                        });
                    }
                });

            }
        };
    });
}());
