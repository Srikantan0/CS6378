package com.os;

public interface SnapshotProtocol {
    // takeSnapshot() -> local state gather -> marker msg -> wait for marker on incoming
    // writeSnapshotToFile() ->
    // isConsistentGlobalState(snapshot) ->
    // areAllNodesPassive() ->
    // areAllChannelsEmpty() ->
    // canTerminate()
    // terminate() / shutdownApplication()
}
