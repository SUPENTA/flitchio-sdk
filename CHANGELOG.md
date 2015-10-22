# Change Log
All notable changes to this project will be documented in this file.
This project adheres to [Semantic Versioning](http://semver.org/).
The Change Log itself follows a standard format described [there](http://keepachangelog.com/).



## [Unreleased][unreleased]



<!--
## [0.7.0] - 2015-??-??
### Added
### Changed
### Deprecated
### Removed
### Fixed
### Security
### Deprecated
-->



## [0.7.0] - 2015-10-22
### Added
- Annotations from Android Support Library for @NonNull, @Nullable and threading.

### Changed
- Important changes concerning status listening
    * A "status" is much more detailed than just true (connected) or false (disconnected): it can now take 5 values and is returned as a `Status` object.
    * The `FlitchioStatusListener` is set in onCreate(), no longer in onResume().
- Because of that, onResume() and onPause() are now mandatory, no matter whether or not you want to receive button and joystick events.
Stating that we have 4 mandatory calls no matter what also makes it clearer to comprehend.
- onCreate() doesn't return a boolean nor throws exceptions.
Instead, errors are reported through a `BINDING_FAILED` status callback with an associated reason.
    
### Fixed
- You're now sure to get a FlitchioStatusListener callback after calling onResume().

### Security
- Added permission check to allow broadcasts only from Flitchio Manager.
No change is needed on the client side.



## [0.6.1] - 2015-09-22
### Fixed
- Exception on onPause() when no FlitchioStatusListener is defined



## [0.6.0] - 2015-09-18
### Added
- FlitchioStatusListener for listening to Flitchio status updates
- Custom stylesheet for Sphinx and Javadoc

### Changed
- Enums to ints for less memory footprint
- FlitchioEventListener now only notifies on button and joystick events
- gh-pages are now generated automatically with Git-Gradle plugin



## [0.5.1] - 2015-08-12
### Added
- Support for Travis CI and README badges
- Google Analytics in documentation

### Changed
- Use latest Gradle plug-in 1.3.0

### Fixed
- Fix version code of Flitchio Manager (was completely broken), now calculated automatically
- Fix links to Javadoc to use Java 8 formatting



## 0.5.0 - 2015-08-10
### Added
- All source code on GitHub: first release.
- All documentation on http://dev.flitch.io.



[unreleased]: https://github.com/SUPENTA/flitchio-sdk/compare/v0.7.0...HEAD
[0.5.1]: https://github.com/SUPENTA/flitchio-sdk/compare/v0.5.0...v0.5.1
[0.6.0]: https://github.com/SUPENTA/flitchio-sdk/compare/v0.5.1...v0.6.0
[0.6.1]: https://github.com/SUPENTA/flitchio-sdk/compare/v0.6.0...v0.6.1
[0.7.0]: https://github.com/SUPENTA/flitchio-sdk/compare/v0.6.1...v0.7.0
