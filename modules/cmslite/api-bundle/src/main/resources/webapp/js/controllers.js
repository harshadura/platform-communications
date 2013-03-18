(function () {

    'use strict';

    /* Controllers */

    var widgetModule = angular.module('motech-cmslite');

    widgetModule.controller('ResourceCtrl', function ($scope, $http, Resources) {
        $scope.resourceType = 'string';
        $scope.resources = [];
        $scope.resources = Resources.query();

        $scope.showType = function (resource) {
            var value = '', type;

            if (resource.type) {
                type = resource.type.toLowerCase();

                if (type.indexOf('string') === 0) {
                    value = 'string';
                } else if (type.indexOf('stream') === 0) {
                    value = 'stream';
                }
            }

            return value;
        }

        $scope.changeResourceType = function (type) {
            $scope.resourceType = type;
        };

        $scope.saveNewResource = function () {
            if ($scope.validateForm('newResourceForm')) {
                blockUI();
                $('#newResourceForm').ajaxSubmit({
                    success: function (data) {
                        $scope.resources = Resources.query();
                        $('#newResourceModal').modal('hide');
                        unblockUI();
                    },
                    error: function (response) {
                        handleWithStackTrace('header.error', 'error.resource.save', response);
                        unblockUI();
                    }
                });
            }
        };

        $scope.validateForm = function (formId) {
            var name = $scope.validateField(formId, 'name'),
                language = $scope.validateField(formId, 'language'),
                value = $scope.validateField(formId, 'value'),
                contentFile = $scope.validateField(formId, 'contentFile');

            return name && language && ($scope.resourceType === 'string' ? value : contentFile);
        };

        $scope.validateField = function (formId, key) {
            var field = $('#' + formId + ' #' + key),
                hint = $('#' + formId + ' #' + key).next('span'),
                validate = field.val() !== undefined && field.val() !== '';

            if (validate) {
                hint.hide();
            } else {
                hint.show();
            }

            return validate;
        };

    });

}());
