'use strict';

describe('Controller: AddflightCtrl', function () {

  // load the controller's module
  beforeEach(module('openflightsApp'));

  var AddflightCtrl,
    scope;

  // Initialize the controller and a mock scope
  beforeEach(inject(function ($controller, $rootScope) {
    scope = $rootScope.$new();
    AddflightCtrl = $controller('AddflightCtrl', {
      $scope: scope
      // place here mocked dependencies
    });
  }));

  it('should attach a list of awesomeThings to the scope', function () {
    expect(AddflightCtrl.awesomeThings.length).toBe(3);
  });
});
