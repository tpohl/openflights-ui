'use strict';

/**
 * @ngdoc function
 * @name openflightsApp.controller:AddflightCtrl
 * @description # AddflightCtrl Controller of the openflightsApp
 */
angular.module('openflightsApp').controller(
		'AddflightCtrl',
		[
				'$scope',
				'$rootScope',
				'BACKEND_URL',
				'$resource',
				'$http',
				function($scope,$rootScope, BACKEND_URL, $resource, $http) {
					var flights = $resource(BACKEND_URL + "/flight", {}, {
						save : {
							method : 'POST',
							headers : {
								'openflightssessionid' : openflightssessionid
							}
						}
					});

					function openflightssessionid(requestConfig) {
						// this function will be called every time the "get"
						// action gets called
						// the result will be used as value for the header item
						// if it doesn't return a value, the key will not be
						// present in the header
						return $rootScope.openflightsSessionId;
					}

					$scope.save = function() {

						flights.save($scope.flight);
					}
					$scope.openCalendar = function(event, what) {
						if ('departure' == what) {
							$scope.cal_open_departure = true;
						} else if ('arrival' == what) {
							$scope.cal_open_arrival = true;
						}
					}

					$scope.autocomplete = function() {
						console.log("autocomplete()")
						$http.post(
								BACKEND_URL + "/flight-autocomplete/flightNo",
								$scope.flight).then(function(response) {
							$scope.flight = response.data;

						}, function() {
							console.log("error")
						});
					};
					$scope.loadMasterdata = function() {
						console.log("masterdata()")
						$http.get(
								BACKEND_URL + "/masterdata/airport/"
										+ $scope.flight.from).then(
								function(response) {
									$scope.airportFrom = response.data
								}, function() {
									console.log("error")
								});
						$http.get(
								BACKEND_URL + "/masterdata/airport/"
										+ $scope.flight.to).then(
								function(response) {
									$scope.airportTo = response.data
								}, function() {
									console.log("error")
								});
						$http.get(
								BACKEND_URL + "/masterdata/aircraft/"
										+ $scope.flight.acType).then(
								function(response) {
									$scope.aircraft = response.data
								}, function() {
									console.log("error")
								});
					}
				} ]

);
