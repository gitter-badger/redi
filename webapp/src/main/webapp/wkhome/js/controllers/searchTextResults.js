wkhomeControllers.controller('searchTextResults', ['$scope', '$window', '$routeParams', 'searchTextResultsService', function ($scope, $window, $routeParams, searchTextResultsService) {
        $scope.candidates = searchTextResultsService.getData();

        $scope.$on('saveData', function () {
            $scope.candidates = searchTextResultsService.getData();
        });


        $scope.selectedOption = function ($event, path, param, paramsQuery) {
            $('#searchResults').modal('hide');
            $('#searchResults').on('hidden.bs.modal', function () {
                $window.location.hash = "/" + $routeParams.lang + path + param;
            });
        };

        $scope.showSubjects = function (values) {
            return _.some(values, 'desc');
        }
    }]);

