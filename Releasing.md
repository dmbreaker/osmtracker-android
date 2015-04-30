Here are the steps to follow for releasing a new version:

  1. Gather changelog by looking at the Changelog wiki page and previous SCM commits.
  1. Pull latest translations from Transifex: `tx pull -a`
  1. Update manifest (versionNumber, versionCode), check that debuggable attribute is false, and commit it.
  1. Export signed application package **using `ant release`**.
  1. Tag version in SCM.
    * `git tag -a 1.2.3`
  1. Upload new APK into market.
    * Change screenshots if necessary.
  1. Update app. market description to include changelog.
  1. Publish update.
  1. Upload APK to the public Google Drive folder.
  1. Update [Changelog](Changelog.md) page.
  1. Check on ToDo and [Limitations](Limitations.md) pages in case some items have been addressed.
  1. Push commits + tag to SCM (For tag: `git push origin 1.2.3`)
  1. Go to the pub