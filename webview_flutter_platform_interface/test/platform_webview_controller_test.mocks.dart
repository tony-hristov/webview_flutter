// Mocks generated by Mockito 5.3.2 from annotations
// in webview_flutter_platform_interface/test/platform_webview_controller_test.dart.
// Do not manually edit this file.

// ignore_for_file: no_leading_underscores_for_library_prefixes
import 'dart:async' as _i4;

import 'package:mockito/mockito.dart' as _i1;
import 'package:webview_flutter_platform_interface/src/platform_navigation_delegate.dart'
    as _i3;
import 'package:webview_flutter_platform_interface/src/webview_platform.dart'
    as _i2;

// ignore_for_file: type=lint
// ignore_for_file: avoid_redundant_argument_values
// ignore_for_file: avoid_setters_without_getters
// ignore_for_file: comment_references
// ignore_for_file: implementation_imports
// ignore_for_file: invalid_use_of_visible_for_testing_member
// ignore_for_file: prefer_const_constructors
// ignore_for_file: unnecessary_parenthesis
// ignore_for_file: camel_case_types
// ignore_for_file: subtype_of_sealed_class

class _FakePlatformNavigationDelegateCreationParams_0 extends _i1.SmartFake
    implements _i2.PlatformNavigationDelegateCreationParams {
  _FakePlatformNavigationDelegateCreationParams_0(
    Object parent,
    Invocation parentInvocation,
  ) : super(
          parent,
          parentInvocation,
        );
}

/// A class which mocks [PlatformNavigationDelegate].
///
/// See the documentation for Mockito's code generation for more information.
class MockPlatformNavigationDelegate extends _i1.Mock
    implements _i3.PlatformNavigationDelegate {
  MockPlatformNavigationDelegate() {
    _i1.throwOnMissingStub(this);
  }

  @override
  _i2.PlatformNavigationDelegateCreationParams get params =>
      (super.noSuchMethod(
        Invocation.getter(#params),
        returnValue: _FakePlatformNavigationDelegateCreationParams_0(
          this,
          Invocation.getter(#params),
        ),
      ) as _i2.PlatformNavigationDelegateCreationParams);
  @override
  _i4.Future<void> setOnNavigationRequest(
          _i3.NavigationRequestCallback? onNavigationRequest) =>
      (super.noSuchMethod(
        Invocation.method(
          #setOnNavigationRequest,
          [onNavigationRequest],
        ),
        returnValue: _i4.Future<void>.value(),
        returnValueForMissingStub: _i4.Future<void>.value(),
      ) as _i4.Future<void>);
  @override
  _i4.Future<void> setOnPageStarted(_i3.PageEventCallback? onPageStarted) =>
      (super.noSuchMethod(
        Invocation.method(
          #setOnPageStarted,
          [onPageStarted],
        ),
        returnValue: _i4.Future<void>.value(),
        returnValueForMissingStub: _i4.Future<void>.value(),
      ) as _i4.Future<void>);
  @override
  _i4.Future<void> setOnPageFinished(_i3.PageEventCallback? onPageFinished) =>
      (super.noSuchMethod(
        Invocation.method(
          #setOnPageFinished,
          [onPageFinished],
        ),
        returnValue: _i4.Future<void>.value(),
        returnValueForMissingStub: _i4.Future<void>.value(),
      ) as _i4.Future<void>);
  @override
  _i4.Future<void> setOnProgress(_i3.ProgressCallback? onProgress) =>
      (super.noSuchMethod(
        Invocation.method(
          #setOnProgress,
          [onProgress],
        ),
        returnValue: _i4.Future<void>.value(),
        returnValueForMissingStub: _i4.Future<void>.value(),
      ) as _i4.Future<void>);
  @override
  _i4.Future<void> setOnWebResourceError(
          _i3.WebResourceErrorCallback? onWebResourceError) =>
      (super.noSuchMethod(
        Invocation.method(
          #setOnWebResourceError,
          [onWebResourceError],
        ),
        returnValue: _i4.Future<void>.value(),
        returnValueForMissingStub: _i4.Future<void>.value(),
      ) as _i4.Future<void>);
}
