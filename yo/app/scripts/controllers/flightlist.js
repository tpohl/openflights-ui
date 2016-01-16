'use strict';

/**
 * @ngdoc function
 * @name openflightsApp.controller:FlightlistCtrl
 * @description # FlightlistCtrl Controller of the openflightsApp
 */
angular.module('openflightsApp').controller(
		'FlightlistCtrl',
		[
				'$scope',
				'$rootScope',
				'BACKEND_URL',
				'$resource',
				'$http',
				function($scope, $rootScope, BACKEND_URL, $resource, $http) {
					var listFlights = $http.get(BACKEND_URL + "/flight/list", $scope.flight)
							.then(function(response) {
								$scope.flights = response.data;

							}, function() {
								console.log("error")
							});
					listFlights();
				} ]);
