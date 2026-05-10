import js from "@eslint/js";
import playwright from "eslint-plugin-playwright";
import tseslint from "typescript-eslint";

export default tseslint.config(
  {
    ignores: ["node_modules/**", ".runtime/**", "playwright-report/**", "test-results/**"]
  },
  js.configs.recommended,
  ...tseslint.configs.recommended,
  {
    files: ["scripts/**/*.js"],
    languageOptions: {
      globals: {
        __dirname: "readonly",
        console: "readonly",
        process: "readonly",
        require: "readonly"
      }
    },
    rules: {
      "@typescript-eslint/no-require-imports": "off",
      "no-console": "off"
    }
  },
  {
    files: ["**/*.ts"],
    rules: {
      "@typescript-eslint/no-unused-vars": [
        "error",
        {
          argsIgnorePattern: "^_",
          varsIgnorePattern: "^_",
          caughtErrorsIgnorePattern: "^_"
        }
      ],
      "@typescript-eslint/no-explicit-any": "error",
      "no-console": ["error", { allow: ["debug", "info", "warn", "error"] }]
    }
  },
  {
    files: ["tests/**/*.ts"],
    ...playwright.configs["flat/recommended"],
    rules: {
      ...playwright.configs["flat/recommended"].rules,
      "playwright/expect-expect": [
        "warn",
        {
          assertFunctionPatterns: [
            "^waitFor",
            "^assert",
            "^verify",
            "^expectPublicSeedAvailable$",
            "^requirePublicSeed$",
            "^assertPublicSeedAvailable$"
          ]
        }
      ],
      "playwright/no-wait-for-timeout": "error",
      "playwright/no-focused-test": "error"
    }
  }
);
