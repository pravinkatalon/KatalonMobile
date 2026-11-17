# MyDigicel Test Automation

Katalon Studio mobile test automation for the MyDigicel application. The project is organized by platform and feature so authentication, account management, payments, and future MyDigicel journeys can share the same profiles, objects, execution conventions, and reporting setup.

## Current automated coverage

- Authentication with notification OTP or password
- Postpaid mobile bill payment scenarios
- Home account selection and bill payment scenarios
- Saved card, new card, duplicate payment, and already paid validations


## Prerequisites

- Katalon Studio 11.4 or a compatible newer release
- A local Appium setup supported by the installed Katalon version
- Android SDK and an Android device/emulator for APK testing
- macOS, Xcode, and an iOS device/simulator when iOS cases are added
- A valid application artifact and test account

Open `Digicel_BillPayment_Automation.prj` from the repository root. The project file retains its historical filename, but the Katalon project name and this repository cover the overall MyDigicel application. The similarly named nested project was a legacy copy and is not canonical.

## Add the application artifact

Application binaries are local-only. The `Apps/` folder is tracked, but its contents are ignored by Git so APK, IPA, app bundles, and signing material are not committed.

Use the naming pattern `mda-<environment>.<extension>`.

| Target | Artifact name example | Profile value |
|---|---|---|
| Local Android | `mda-local.apk` | `Apps/mda-local.apk` |
| Staging Android | `mda-staging.apk` | `Apps/mda-staging.apk` |
| Production Android | `mda-prod.apk` | `Apps/mda-prod.apk` |
| Staging physical iOS device | `mda-staging.ipa` | `Apps/mda-staging.ipa` |
| Staging iOS simulator | `mda-staging.app` | `Apps/mda-staging.app` |

1. Download or obtain the application artifact.
2. Rename it using the pattern above. For example, rename a staging Android build to `mda-staging.apk`.
3. Copy or symlink it into `Apps/`.
4. Select the matching execution profile in Katalon Studio.
5. If the extension or filename differs from the profile default, update `G_AppPath` in that profile.

Example:

```bash
cp ~/Downloads/mydigicel.apk Apps/mda-staging.apk
```

To avoid copying a large local artifact:

```bash
ln -s ~/Downloads/mydigicel.apk Apps/mda-staging.apk
```

The current Android login tests resolve `G_AppPath` relative to the project root and fail with a clear message when the file is missing or is not an `.apk`. The IPA and `.app` naming examples reserve the same layout for future iOS cases.

### Platform notes

- Use an APK with an Android device or emulator.
- Use a correctly signed and provisioned IPA with a compatible physical iOS device. The device must trust the signing identity/profile.
- An iOS simulator cannot install an IPA. Use a simulator-compatible `.app` bundle instead.
- Katalon Mobile Recorder or Mobile Spy can select a local artifact for recording. Saved project tests still launch the path configured by `G_AppPath`.

## Configure execution profiles

The project includes:

- `default`: local fallback, with `G_AppPath` set to `Apps/mda-local.apk`
- `staging`: staging configuration, with `G_AppPath` set to `Apps/mda-staging.apk`

Each profile defines:

| Variable | Purpose | Stored in Git |
|---|---|---|
| `G_AppPath` | Project-relative or absolute application artifact path | Yes |
| `G_MSISDN` | Subscriber number used by the login flow | Yes |
| `G_Password` | Password used when `G_UseOTP` is `false` | Yes |
| `G_UseOTP` | `true` for OTP login; `false` for password login | Yes |
| `G_AlternateMSISDN` | Different subscriber used only by `TC_BILL_ALREADY_PAID` | Yes |
| `G_AlternatePassword` | Alternate subscriber password when `G_AlternateUseOTP` is `false` | Yes |
| `G_AlternateUseOTP` | Authentication mode used only by `TC_BILL_ALREADY_PAID` | Yes |
| `G_HomeAccount` | Primary Home account name or account number used for account selection | Yes |
| `G_AlternateHomeAccount` | Different Home account used by multi-account scenarios | Yes |

In Katalon Studio:

1. Open **Profiles** and select the profile to configure.
2. Enter `G_MSISDN` locally.
3. Set `G_UseOTP` to `true` for OTP authentication or `false` for password authentication.
4. Enter `G_Password` when `G_UseOTP` is `false`.
5. Enter `G_AlternateMSISDN` and choose `G_AlternateUseOTP` before running either `TC_BILL_ALREADY_PAID`; enter `G_AlternatePassword` when its mode is password.
6. Enter `G_HomeAccount` before running Home scenarios.
7. Enter `G_AlternateHomeAccount` when running `TC_SAME_AMOUNT_DIFFERENT_ACCOUNT`.
8. Confirm `G_AppPath` matches the renamed file in `Apps/`.
9. Select the profile from the execution-profile menu before running a test case or suite.

These variables are not protected. Values entered in a profile are stored as plain text in its `.glbl` file, so review them before committing the profile.

## Project structure

```text
Apps/                    Local application artifacts (ignored)
Object Repository/
  Android/
    Authentication/     Authentication objects
    Bill Payment/       Current payment-feature objects
Profiles/                Default and staging configuration
Scripts/                 Katalon-generated script storage
Test Cases/
  Android/
    Authentication/
    Bill Payment/
    Home/
Test Suites/
  Android/
    TSC_ANDROID_REGRESSION
    Authentication/
    Bill Payment/
```

## Naming conventions

- Platform folders use title case, currently `Android`.
- Feature folders under a platform use title case and spaces: `Authentication`, `Bill Payment`, `Home`.
- Test-case names, filenames, and script folders use `UPPER_SNAKE_CASE` with a `TC_` prefix, for example `TC_LOGIN_WITH_OTP` and `TC_PAY_INVOICE_SAVED_CARD`.
- Test-suite names and filenames use `UPPER_SNAKE_CASE` with a `TS_` prefix and are grouped under their platform, for example `Test Suites/Android/Bill Payment/TS_BILL_PAYMENT_POSTPAID`.
- Test objects are grouped under `Object Repository/<Platform>/<Feature>` and use lower snake case with semantic prefixes such as `btn_`, `input_`, `txt_`, `tab_`, `toggle_`, `radio_`, `icon_`, and `card_`.
- Katalon-managed `Script<timestamp>.groovy` filenames are not renamed manually.
- Folder names and references are case-sensitive on CI and non-macOS systems.

## Suites and execution

| Entity | Scope |
|---|---|
| `TSC_ANDROID_REGRESSION` | All current Android feature suites, run sequentially |
| `TS_LOGIN` | Profile-selected OTP or password authentication |
| `TS_BILL_PAYMENT_POSTPAID` | Postpaid mobile account payment coverage |
| `TS_BILL_PAYMENT_HOME` | Home account payment coverage |

1. Start or connect the required Android device or emulator.
2. Open the project in Katalon Studio.
3. Select `default` or `staging`.
4. Confirm the matching renamed artifact exists in `Apps/`.
5. For the complete regression execution, run `Test Suites/Android/TSC_ANDROID_REGRESSION`.
6. To run a smaller scope, run an individual test case or one platform/feature suite.

Tests that require authentication call `TC_LOGIN`, which routes to `TC_LOGIN_WITH_OTP` when `G_UseOTP` is `true` and `TC_LOGIN_WITH_PASSWORD` when it is `false`. Both login paths leave the authenticated app open for the dependent scenario. This keeps feature tests independently runnable while centralizing login selection. `TC_BILL_ALREADY_PAID` passes `G_AlternateMSISDN`, `G_AlternatePassword`, and `G_AlternateUseOTP` as overrides, so it uses a separate account without changing the login used by any other test.

Home scenarios select `G_HomeAccount`; the different-account scenario selects `G_AlternateHomeAccount` for its second payment.

## Reports and recordings

Run a test suite or `TSC_ANDROID_REGRESSION` when a complete execution report is required. Before running the collection, open it and confirm every suite row is enabled and uses the intended execution profile. The collection currently runs authentication and all implemented Android feature suites sequentially.

Katalon's built-in browser recorder is disabled because it cannot record local mobile execution. The `ANDROID_VIDEO_RECORDER` test listener instead uses Android `screenrecord` to capture one MP4 for each top-level test case. Called helper cases remain part of their parent recording so nested `callTestCase` steps cannot interrupt it.

Recordings are stored under the active execution report:

```text
Reports/<execution>/recordings/
├── 001_TC_LOGIN_20260910_111500_000.mp4
├── 002_TC_PAY_ENTERED_AMOUNT_SAVED_CARD_20260910_111620_000.mp4
└── ...
```

The three-digit prefix is the test-case execution order. A timestamp prevents retries and repeated runs from overwriting earlier evidence. The listener automatically:

- Detects the single connected Android device or emulator.
- Records passed and failed top-level test cases.
- Stops and pulls the video after each test case.
- Deletes the device-side copy to protect emulator storage.

Local recording prerequisites and constraints:

- `adb` must be available through `ANDROID_SDK_ROOT`, `ANDROID_HOME`, Katalon's bundled Android SDK, or `PATH`.
- Exactly one Android device or emulator must be connected.
- Each recording is capped at three minutes so a manually stopped Katalon run cannot leave an unlimited recorder consuming emulator storage. Test cases longer than three minutes have a partial video.
- The next test-case execution removes orphaned `katalon_*.mp4` files and recorder processes left by a force-stopped run.
- Do not run mobile cases in parallel against the same device.
- Videos can contain credentials, OTPs, CVVs, and customer information. Restrict access and retention accordingly.

After execution finishes:

1. Open **Reports** in Tests Explorer and select the latest run.
2. Confirm every enabled suite and expected test case has a result.
3. Review status, duration, execution logs, screenshots, and any available videos.
4. Export the report or use the configured Katalon True Platform upload when a shareable result is required.

The local `Reports/` directory is generated automatically and excluded from Git. Recordings can consume significant disk space, so archive required evidence and periodically remove obsolete local reports.

`G_UseOTP` selects one authentication branch per execution. Run the collection once with `G_UseOTP=true` and again with `G_UseOTP=false` when the report must demonstrate coverage of both login methods. A disabled suite row or test-case link is not executed and therefore cannot appear in the report.

## Troubleshooting

- **Application artifact not found:** verify the file was renamed and placed in `Apps/`, then verify `G_AppPath`.
- **MSISDN is empty:** open the selected profile and enter `G_MSISDN`.
- **Already-paid login is empty:** configure `G_AlternateMSISDN` and either enable `G_AlternateUseOTP` or enter `G_AlternatePassword`.
- **IPA does not install on a simulator:** use a simulator `.app` bundle; IPA files target physical devices.
- **iOS installation fails:** verify signing, provisioning, device trust, and bundle compatibility.
- **Object or test case not found:** check exact folder/name casing and avoid moving entities outside Katalon Studio without updating references.
- **OTP is not found:** confirm notification access is enabled and the expected OTP notification was delivered.
- **A test is missing from the report:** confirm its suite link and collection row are enabled before execution.
- **A mobile video is missing:** confirm exactly one device appears as `device` in `adb devices`, inspect listener warnings in the execution log, and verify the test lasted long enough for Android to finalize a non-empty MP4.
- **Reports consume too much disk space:** archive required evidence, then remove obsolete generated runs from the ignored `Reports/` directory.

## Katalon documentation

- [About Katalon Studio](https://docs.katalon.com/katalon-studio/about-katalon-studio)
- [Global variables and execution profiles](https://docs.katalon.com/katalon-studio/data-driven-testing/global-variables-and-execution-profile)
- [Generate screen-based videos in Katalon Studio reports](https://docs.katalon.com/katalon-studio/test-reports/generate-test-reports/generate-screen-based-videos-in-katalon-studio-reports)
