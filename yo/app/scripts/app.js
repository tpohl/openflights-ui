'use strict';

/**
 * @ngdoc overview
 * @name openflightsApp
 * @description
 * # openflightsApp
 *
 * Main module of the application.
 */
angular
  .module('openflightsApp', [
    'ngAnimate',
    'ngCookies',
    'ngResource',
    'ngRoute',
    'ngSanitize',
    'ngTouch',
    'ui.bootstrap',
    'ui.bootstrap.datetimepicker'
  ])
  .config(function ($routeProvider) {
    $routeProvider
      .when('/', {
        templateUrl: 'views/main.html',
        controller: 'MainCtrl',
        controllerAs: 'main'
      })
      .when('/about', {
        templateUrl: 'views/about.html',
        controller: 'AboutCtrl',
        controllerAs: 'about'
      })
      .when('/addFlight', {
        templateUrl: 'views/addflight.html',
        controller: 'AddflightCtrl',
        controllerAs: 'addFlight'
      })
      .otherwise({
        redirectTo: '/'
      });
  });
