/*eslint-env node, mocha*/
(function() {

    'use strict';

    var rewire = require("rewire");
    var assert = require('chai').assert;
    var sinon = require('sinon');

/*
    var mockcloudant = {
        db: {
            create: function(key, callback) {
                callback( false, "woot" );
            }
        }
    };
*/
    var mockdb = {
        ordersDb: {
            insert: function(body, callback){
                callback(null);
            },
            get: function(id, obj, callback) {
                callback(null, "body");
            }
        },
        cloudant: {
            db: {
                create: function(key, callback) {
                    console.log("mock called")
                    callback( false, "woot" );
                }
            }
        }
        
    };    

    global.cloudantService = {
        url: "https://abc"
    };
    
    //var cdb = rewire('../../tests/server/coverage/instrumented/routes/db.js');
    //cdb.__set__('cloudant', mockcloudant);
    //cdb.__set__('itemsDb', mockdb);
    var orders = rewire('../../tests/server/coverage/instrumented/routes/orders.js');
    //orders.__set__('cloudant', mockcloudant);
    orders.setTestMode(true);
    orders.__set__('ordersDb', mockdb.ordersDb);
    orders.__set__('cloudant', mockdb.cloudant);

    var USE_FASTCACHE = orders.getFastCache();

    // create mock request and response
    var reqMock = {
        params: {
            
        }
    };

    var resMock = {};
    resMock.status = function() { return this;};
    resMock.send = function() {};
    resMock.end = function() {};
    sinon.spy(resMock, "send");
    
    describe('create Function', function() {
        it('Order created successfully', function() {
            orders.create( reqMock, resMock );
            assert( resMock.send.lastCall.calledWith( { msg: 'Successfully created item' } ), 'Unexpected argument: ' + JSON.stringify(resMock.send.lastCall.args) );
        });
        
        it('Order not created - db error', function() {
            mockdb.ordersDb.insert = function( key, callback ){
                callback('forced error');  
            };
    
            orders.create( reqMock, resMock );        
            assert( resMock.send.lastCall.calledWith( { msg: 'Error on insert, maybe the item already exists: forced error' } ), 'Unexpected argument: ' + JSON.stringify(resMock.send.lastCall.args) );
        });
    });
    
    describe('find Function', function() {
        it('Order found successfully', function() {
            reqMock.params.id = 'testId';
            mockdb.ordersDb.get = function( id, arg, callback ){
                callback( false, 'test body' );  
            };
            
            orders.find( reqMock, resMock );
            if(USE_FASTCACHE) {
                assert( resMock.send.lastCall.calledWith( {msg:"server error"} ), 'Unexpected argument: ' + JSON.stringify(resMock.send.lastCall.args) );
            } else {
                assert( resMock.send.lastCall.calledWith( 'test body' ), 'Unexpected argument: ' + JSON.stringify(resMock.send.lastCall.args) );
            }
        });
        
        it('Order not found - db error', function() {
            reqMock.params.id = 'testId';
            mockdb.ordersDb.get = function( id, arg, callback ){
                callback( 'forced error', '' );  
            };
            
            orders.find( reqMock, resMock );
            if(USE_FASTCACHE) {
                assert( resMock.send.lastCall.calledWith( {msg:"server error"} ), 'Unexpected argument: ' + JSON.stringify(resMock.send.lastCall.args) );
            } else {
                assert( resMock.send.lastCall.calledWith( { msg: 'Error: could not find item: testId' } ), 'Unexpected argument: ' + JSON.stringify(resMock.send.lastCall.args) );
            }
        });
    });
    
    describe('list Function', function() {
        it('All Db content listed successfully', function() {
            mockdb.ordersDb.list = function( arg, callback ){
                callback( false, 'test body', 'headers' );  
            };
            
            orders.list( reqMock, resMock );
            assert( resMock.send.lastCall.calledWith( 'No orders logged' ), 'Unexpected argument: ' + JSON.stringify(resMock.send.lastCall.args) );
        });
        
        it('Db content not listed - db error', function() {
            mockdb.ordersDb.list = function( arg, callback ){
                callback( 'forced error', 'test body', 'headers' );  
            };
            
            orders.list( reqMock, resMock );
            assert( resMock.send.lastCall.calledWith( { msg: "'list' failed: forced error" } ), 'Unexpected argument: ' + JSON.stringify(resMock.send.lastCall.args) );
        });
    });
}());
