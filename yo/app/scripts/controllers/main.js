'use strict';

/**
 * @ngdoc function
 * @name openflightsApp.controller:MainCtrl
 * @description # MainCtrl Controller of the openflightsApp
 */
angular
		.module('openflightsApp')
		.controller(
				'MainCtrl',
				[
						'$scope',
						'BACKEND_URL',
						'$rootScope',
						'$http',
						function($scope, BACKEND_URL, $rootScope, $http) {
							$scope.login = function() {
								var name = $scope.username;
								var pw = $scope.password;
								
								var c = {};
								c.username = $scope.username;
								c.password = pw;
								
								$http
										.post(BACKEND_URL + "/login", c)
										.then(
												function(response) {
													$rootScope.openflightsSessionId = response.data.sessionId;
													$http.defaults.headers.common['openflightssessionid'] = $rootScope.openflightsSessionId;
													$scope.password = "";
												}, function() {
													console.log("error")
												});
							}

						} ]);
