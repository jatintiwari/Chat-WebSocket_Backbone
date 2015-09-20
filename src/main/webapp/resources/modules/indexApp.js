require .config({

    baseUrl: 'resources/modules',

    paths: {
        backbone:   [
                     '../lib/backbone/backbone-min'],
                   
        underscore: [
                     '../lib/backbone/underscore'],
                     
        jquery:   [
                   '../lib/js/jquery'],
                   
        text:   [
                 '../lib/require/text'],
                 
        bootstrap:  [
                     '../lib/js/bootstrap.min'],
        chart:	['//cdnjs.cloudflare.com/ajax/libs/Chart.js/1.0.2/Chart.min','../lib/chart/Chart.min']
    },
    
    shim: {
        'backbone': {
            deps: ['underscore', 'jquery'],
            exports: 'Backbone'
        },
        'underscore': {
            exports: '_'
        },
        'bootstrap': {
          deps: ['jquery']
        },
        
    },
    
    waitSeconds: 0
});

require(['jquery', 'backbone','indexRouter','bootstrap'], function ($, Backbone,IndexRouter,Bootstrap) {
	indexRouter = new IndexRouter();
  Backbone.history.start();
});