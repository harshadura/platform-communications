(function () {
    'use strict';

    var smsModule = angular.module('motech-sms');

    smsModule.controller('TestController', function ($scope, $http, TestService) {
        $scope.sms = {};

        $http.get('../sms/settings')
            .success(function(res){
                var key;
                $scope.settings = res;
                for (key in $scope.settings.configs) {
                    if ($scope.settings.configs[key]['default'] === 'true') {
                        $scope.sms.config = $scope.settings.configs[key].name;
                         break;
                    }
                }
            });

        $scope.sendSms = function () {

            TestService.save(
                {},
                $scope.sms,
                function () {
                    motechAlert('sms.test.alert.success', 'sms.test.alert.title');
                },
                function (response) {
                    handleWithStackTrace('sms.test.alert.title', 'sms.test.alert.failure', response);
                }
            );
        };
    });


    smsModule.controller('LogController', function ($scope, $http) {
        $scope.log = {};
/*
        $http.get('../sms/settings')
            .success(function(res){
                var key;
                $scope.settings = res;
                for (key in $scope.settings.configs) {
                    if ($scope.settings.configs[key]['default'] === 'true') {
                        $scope.sms.config = $scope.settings.configs[key].name;
                         break;
                    }
                }
            });

        $scope.sendSms = function () {

            SendSmsService.save(
                {},
                $scope.sms,
                function () {
                    motechAlert('sms.test.alert.success', 'sms.test.alert.title');
                },
                function (response) {
                    handleWithStackTrace('sms.test.alert.title', 'sms.test.alert.failure', response);
                }
            );
        };
*/
    });


    smsModule.controller('SettingsController', function ($scope, $http, SettingService, TemplateService) {

        //TODO: figure out a way for directives.js to use a this array
        $scope.reservedProperties = ['name', 'template', 'default', 'openAccordion'];

        $http.get('../sms/settings')
            .success(function(res){
                var i;
                $scope.settings = res;
                $scope.originalSettings = angular.copy($scope.settings);
                $scope.accordions = [];
                for (i=0 ; i< $scope.settings.configs.length ; i = i + 1) {
                    $scope.accordions.push(false);
                }
            });

        $scope.templates = TemplateService.get();

        /* TODO

            This replaces the configuration's properties with the ones from the selected template.
            Do we want to 'remember' the old template properties in case the user chooses to select the old template
            back from the dropdown?
        */
        $scope.changeTemplateProperties = function (config) {
            var key;
            //remove the previous template's properties
            for (key in config) {
                if ($scope.reservedProperties.indexOf(key) === -1) {
                    delete config[key];
                }
            }
            //insert the new template's properties
            for (key in $scope.templates[config.template]) {
                if ($scope.reservedProperties.indexOf(key) === -1 && key.substring(0,5) !== "user.") {
                    config[key] = $scope.templates[config.template][key];
                }
            }

        };

        $scope.collapseAccordions = function () {
            var key;
            for (key in $scope.accordions) {
                $scope.accordions[key] = false;
            }
        };

        $scope.reset = function () {
            $scope.settings = angular.copy($scope.originalSettings);
            $scope.collapseAccordions();
        };

        $scope.setDefault = function (index) {
            var i;
            for (i = 0 ; i < $scope.settings.configs.length ; i = i + 1) {
                if (i === index) {
                    $scope.settings.configs[i]['default'] = 'true';
                }
                else {
                    $scope.settings.configs[i]['default'] = 'false';
                }
            }
        };

        $scope.setNewDefault = function () {
            var i;
            for (i = 0 ; i < $scope.settings.configs.length ; i = i + 1) {
                if ($scope.settings.configs[i]['default'] === 'true') {
                    return;
                }
            }
            $scope.settings.configs[0]['default'] = 'true';
        };

        $scope.addConfig = function () {
            $scope.settings.configs.push({'name':'Untitled', 'template':'', 'default':'false'});
            $scope.accordions.push(true);
            $scope.setNewDefault();
        };

        $scope.isDirty = function () {
            return !angular.equals($scope.originalSettings, $scope.settings);
        };

        $scope.submit = function () {

            SettingService.save(
                {},
                $scope.settings,
                function () {
                    $scope.collapseAccordions();
                    $scope.originalSettings = SettingService.get();
                },
                function (response) {
                    handleWithStackTrace('sms.header.error', 'server.error', response);
                }
            );
        };
    });
}());
