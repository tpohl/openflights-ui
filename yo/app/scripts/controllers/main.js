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
								// TODO get the correct challenge
								var challenge = '77cbbbb2592d76e212f6d65ad0fba2c2';
								var hash = hex_md5(challenge
										+ hex_md5(pw + name.toLowerCase()));
								var legacy_hash = hex_md5(challenge
										+ hex_md5(pw + name));
								alert(hash);
								var c = {};
								c.username = $scope.username;
								c.passwordHash = hash;
								c.challenge = challenge
								$http
										.post(BACKEND_URL + "/login", c)
										.then(
												function(response) {
													$rootScope.openflightsSessionId = response.data.sessionId;

												}, function() {
													console.log("error")
												});
							}

						} ]);
