// packages/ui/rspack.config.js
const { Configuration } = require('@rspack/core');
const { resolve } = require('path');
const { TsCheckerRspackPlugin } = require('ts-checker-rspack-plugin');
module.exports = {
  target: 'node', // React Native 需要 Node 目标
  context: __dirname,
  entry: {
    main: './src/index.ts'
  },
  output: {
    path: resolve(__dirname, 'dist'),
    filename: '[name].js',
    library: {
      type: 'commonjs2'
    }
  },
  resolve: {
    extensions: ['.tsx', '.ts', '.jsx', '.js'],
    alias: {
      'react-native$': 'react-native-web' // Web 到 Native 的兼容层
    }
  },
  module: {
    rules: [
      {
        test: /\.(js|jsx|ts|tsx)$/,
        use: {
          loader: 'builtin:swc-loader',
          options: {
            jsc: {
              parser: {
                syntax: 'typescript',
                tsx: true
              },
              transform: {
                react: {
                  runtime: 'automatic',
                  importSource: 'react'
                }
              }
            }
          }
        }
      }
    ]
  },
  plugins: [new TsCheckerRspackPlugin()],
  externals: {
    react: 'commonjs react',
    'react-native': 'commonjs react-native'
  },
};