# Android - Custom linting

This repository objective is to play with custom linting for our Android project. Why, should we do that ?
It can be a great way to share common guideline inside a team.

## ViewBinding memory leaks detector

To give you an example of useful rule. You can find in this project a way to detect memory leaks while
using ViewBinding inside Fragment. If you don't know what I'm talking about, I will let you with [this article](https://proandroiddev.com/avoiding-memory-leaks-when-using-data-binding-and-view-binding-3b91d571c150).

## How to test your custom rule

To be able to see your rules in action, you need to pass the lint on your project. For example, here I run the following command :

```
./gradlew :app:lintDebug
```

## Great sources to read

- [Android Lint API Guide](http://googlesamples.github.io/android-custom-lint-rules/api-guide.html#writingalintcheck:basics/analyzingkotlinandjavacode/uast)
- [Enforcing Team Rules with Lint: Detectors](https://zarah.dev/2020/11/19/todo-detector.html)
- [Enforcing Clean Architecture Using Android Custom Lint Rules](https://proandroiddev.com/enforcing-clean-architecture-using-android-custom-lint-rules-aa8fc1708c59)
