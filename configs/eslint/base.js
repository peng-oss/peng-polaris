module.exports = {
  extends: [
    "eslint:recommended",
    "plugin:react/recommended",
    "plugin:react-native/all"
  ],
  plugins: ["react", "react-native"],
  parserOptions: {
    ecmaVersion: "latest",
    sourceType: "module",
    ecmaFeatures: {
      jsx: true
    }
  },
  rules: {
    "react/react-in-jsx-scope": "off",
    "react-native/no-raw-text": ["warn", { "skip": ["Typography"] }]
  },
  settings: {
    react: {
      version: "detect"
    }
  },
  env: {
    "react-native/react-native": true
  }
}