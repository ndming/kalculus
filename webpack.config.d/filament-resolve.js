// noinspection JSUnresolvedVariable
config.resolve.fallback = {
    // "fs": false,
    // "buffer" : false,
    // "stream" : false,
    "path": require.resolve("path-browserify"),
    "fs" : require.resolve("fs-extra"),
    "crypto": require.resolve("crypto-browserify"),
    "stream": require.resolve("stream-browserify"),
    "buffer": require.resolve("buffer/"),
    "util": require.resolve("util/"),
    "assert": require.resolve("assert/"),
    "constants": require.resolve("constants-browserify")
}