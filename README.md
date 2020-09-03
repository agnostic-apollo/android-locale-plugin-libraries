# android-locale-plugin-libraries

This is a temp repository for the [twofortyfouram locale libraries] that have been patched so that the plugin apps can be built for `targetSdkVersion 29`. The aim is to make the plugin libraries a plug-and-play solution for creating [Tasker App] plugins instead of requiring custom patches, making it easier for new users to create plugins and also for existing app devs to integrate plugin functionality into their apps. The upstream repos are available on [twofortyfouram's github]. Currently, the patched libraries are **experimental** and more works needs to be done, specially for [testLib], but that is not required for building plugin apps. The other libraries can still be used to build plugin apps and who needs testings anyways. Like the great Linus Torvalds says:

> "Regression testing"? What's that? If it compiles, it is good; if it boots up, it is perfect.

Just kidding, testing is important, but patching that library may be beyond my skill level.

I am also currently busy with other projects and hence do not have the free time to do more work on this to get the patches into upstream repos but will come back when I can. I am dumping the code for others in case they need to use it. The [TaskerLauncherShortcut] plugin app uses these libraries and you can see the usage guides there to create your own plugins.

If you want a possibly more stable alternate plugin library for [Tasker App] with examples, you can take a look at [TaskerPluginSample], created by the joaomgcd, the current owner of [Tasker App]. You can check its documentation [here](https://tasker.joaoapps.com/pluginslibrary.html).

You can find the general [Tasker App] plugin documentation intro [here](https://tasker.joaoapps.com/plugins-intro.html) and details [here](https://tasker.joaoapps.com/plugins.html).
##


### Contents
- [Compatibility](#Compatibility)
- [Downloads](#Downloads)
- [Usage](#Usage)
- [Patches](#Patches)
- [Current Work Done](#Current-Work-Done)
- [Future Work](#Future-Work)
- [Issues](#Issues)
- [Worthy Of Note](#Worthy-Of-Note)
- [FAQs And FUQs](#FAQs-And-FUQs)
- [Changelog](#Changelog)
- [Contributions](#Contributions)
##


### Compatibility

- Android `minSdkVersion 16` and `targetSdkVersion 29`.
##


### Downloads

- [GitHub releases](https://github.com/agnostic-apollo/android-locale-plugin-libraries/releases).
##


### Patches

The patch files are only for `src` directories and do not include other directories and files in root directory of the libraries like `build.gradle`.

- [annotationLib.patch](patches/annotationLib.patch)
- [assertionLib.patch](patches/assertionLib.patch)
- [pluginApiLib.patch](patches/pluginApiLib.patch)
- [pluginClientSdkLib.patch](patches/pluginClientSdkLib.patch)
- [pluginHostSdkLib.patch](patches/pluginHostSdkLib.patch)
- [spackleLib.patch](patches/spackleLib.patch)
- [testLib.patch](patches/testLib.patch)
##


### Current Work Done

- Compatible with `targetSdkVersion 29`
- Raised `minSdkVersion` from `9` to `16`. (Don't remember exactly why, could have been required)
- Androidx Support
- Better support for [Tasker App] `EditActivity` and `FireReceiver` intents.
##


### Future Work

- Look into Event plugins and [IntentServices](https://tasker.joaoapps.com/pluginsservicesmigration.html) support
- Create some basic example plugin apps
- Updating deprecated code in [testLib] and test code of other libraries
##


### Issues

`-`
##


### Worthy Of Note

`-`
##


### FAQs And FUQs

Check [FAQs_And_FUQs.md](FAQs_And_FUQs.md) file for the **Frequently Asked Questions(FAQs)** and **Frequently Unasked Questions(FUQs)**.
##


### Changelog

Check [CHANGELOG.md](CHANGELOG.md) file for the **Changelog**.
##


### Contributions

`-`
##


[Tasker App]: https://play.google.com/store/apps/details?id=net.dinglisch.android.taskerm
[TaskerPluginSample]: https://github.com/joaomgcd/TaskerPluginSample
[TaskerLauncherShortcut]: https://github.com/agnostic-apollo/TaskerLauncherShortcut
[twofortyfouram locale libraries]: https://www.twofortyfouram.com/developer
[twofortyfouram's github]: https://github.com/twofortyfouram
[testLib]: https://github.com/twofortyfouram/android-test
