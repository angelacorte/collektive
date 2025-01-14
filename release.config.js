const publishCmd = `
./gradlew plugin:gradle-plugin:publishPlugins -Pgradle.publish.key=$GRADLE_PUBLISH_KEY -Pgradle.publish.secret=$GRADLE_PUBLISH_SECRET || exit 1
./gradlew -PstagingRepositoryId=\${process.env.STAGING_REPO_ID} releaseStagingRepositoryOnMavenCentral || exit 2
`;

const config = require('semantic-release-preconfigured-conventional-commits');
config.plugins.push(
  [
    "@semantic-release/exec",
    {
      "publishCmd": publishCmd,
    }
  ],
  "@semantic-release/github",
  "@semantic-release/git",
)
module.exports = config
