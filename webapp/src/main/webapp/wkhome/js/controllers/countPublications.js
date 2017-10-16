wkhomeControllers.controller('countPublications', ['$window', '$routeParams', '$scope', 'searchData', 'globalData', 'sparqlQuery', 'Statistics',
  function($window, $routeParams, $scope, searchData, globalData, sparqlQuery, Statistics) {

    //if click in pie-chart (Authors)
    $scope.ifClick = function(value) {
      searchData.genericData = value;
      $window.location.hash = "/" + $routeParams.lang + "/w/cloud?" + "datacloud";
    };

    Statistics.query({
      id: 'count_publications'
    }, function(data) {
      var endpoints = data['@graph'];
      var data = []
      endpoints.forEach(function(endpoint) {
        var name = endpoint['uc:name'];

        data.push({
          label: name,
          value: endpoint['uc:total']['@value']
        });

      });
      $scope.data = {
        'entityName': 'Articles',
        'data': data
      };
    });

    // var queryTotalAuthors =   globalData.PREFIX
    //         + ' CONSTRUCT '
    //         + ' {  '
    //         + '     ?prov uc:total ?totalp.'
    //         + '     ?prov uc:name ?sname.'
    //         + ' } '
    //         + ' WHERE {'
    //         + ' {'
    //         + '     SELECT DISTINCT (SAMPLE(?provenance)  as ?prov) (SAMPLE(?sourcename)  as ?sname)  (count(DISTINCT ?pub) as ?totalp)'
    //         + '         WHERE {'
    //         + '             GRAPH <' + globalData.centralGraph + '> {'
    //         + '                 ?s a foaf:Person.'
    //         + '                 ?s foaf:name ?name.'
    //         + '                 ?s foaf:publications ?pub.'
    //         + '                 ?s dct:provenance ?provenance.     '
    //         + '                 { '
    //         + '                     SELECT * '
    //         + '                     WHERE '
    //         + '                     { '
    //         + '                         graph <' + globalData.endpointsGraph + '> '
    //         + '                         { '
    //         + '           			?provenance uc:name ?sourcename '
    //         + '                         } '
    //         + '                     } '
    //         + '                 } '
    //         + '             }'
    //         + '         }group by ?provenance '
    //         + ' } '
    //         + ' }';
    // sparqlQuery.querySrv({query: queryTotalAuthors}, function (rdf) {
    //     jsonld.compact(rdf, globalData.CONTEXT, function (err, compacted) {
    //         //$scope.data = compacted;
    //         var endpoints = compacted['@graph'];
    //         var data = []
    //         endpoints.forEach(function (endpoint) {
    //             var name = endpoint['uc:name'];
    //
    //                     data.push({label: name, value: endpoint['uc:total']['@value']});
    //
    //         });
    //         $scope.$apply(function () {
    //             $scope.data = {'entityName': 'Articles', 'data': data};
    //         });
    //     });
    // });
  }
]);
