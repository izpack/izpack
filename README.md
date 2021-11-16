# IzPack
[![Java CI with Maven](https://github.com/izpack/izpack/actions/workflows/maven.yml/badge.svg)](https://github.com/izpack/izpack/actions/workflows/maven.yml)
[![Apache License 2.0](https://img.shields.io/badge/license-Apache%20License%202.0-blue)](http://www.apache.org/licenses/LICENSE-2.0)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.codehaus.izpack/izpack-core/badge.svg)](https://search.maven.org/search?q=g:org.codehaus.izpack)
[![Java 8](https://img.shields.io/badge/java-8-blue.svg)](https://adoptium.net/)

[IzPack](http://izpack.org/) is a widely used tool for packaging applications on the Java platform as cross-platform installers.

## License

IzPack is published under the terms of the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0), meaning that you can adapt it to your needs with very minimal constraints.

Some third-party components (e.g., look and feel libraries) may be released
under different terms.

## Building IzPack from source

IzPack requires Java SE 1.8 and at minimum Maven 3. Due to the JDK compatibility the maximum Maven version supported is 3.6.3 when compiled using JDK 1.8.

    mvn verify install

The build generates a distribution IzPack installer JAR in `izpack-dist/target`.

The IzPack Maven plugin is inside the `izpack-maven-plugin` module.

## Contributing to IzPack

While reporting an issue [on our JIRA tracker](https://izpack.atlassian.net/) is useful, investigating and offering a patch is much better!

We suggest that [you follow our guidelines for contributing](http://izpack.org/developers/), and especially that you have a fork of [https://github.com/izpack/izpack](https://github.com/izpack/izpack) on GitHub. You can then offer contributions using pull requests.

We very much prefer pull requests over attaching patches in a JIRA issues.

## Resources

During the migration from the Codehaus services, which were shut down, we had do divide different services to separate providers, there hasn't been available a compact offer comparable to the services the Codehaus in such a short time. There are some smaller drawbacks which couldn't be avoided, see below.

If you are wondering where to find some service the project offers visit out web site http://izpack.org/.
This domain is still kindly provided and paid by Julien Ponge, the project founder.

The most recent snapshots builds based on the `master` branch are available on the Soantype Server:

[https://oss.sonatype.org/content/repositories/snapshots/org/codehaus/izpack/](https://oss.sonatype.org/content/repositories/snapshots/org/codehaus/izpack/)

Below I provide a list of new services divided by providers:

### IzPack@Github

If you want to participate in development or improving our website, grab a Github account and send pull requests. All you need is the basic idea and the knowledge in using GIT and the Github services.

Don't use the Github issue tracker or Wiki at this time. We use more advanced services for this.

#### Source code

The IzPack source code is hosted at https://github.com/izpack/izpack.
See the Developing and contributing section about how to contribute code changes.

Fork the Github repository, create a branch using an according JIRA issue as the branch name for each change and send pull requests from this branch to be able to create a changelog for each release and not get mixed up changes from your master branch for several issues at once.

#### Web page

The IzPack website is hosted at https://github.com/izpack/izpack.github.com. 
For bigger changes or refactory use a special branch for each change. Send pull requests. 

The resulting website izpack.github.com is directly forwarded to izpack.org by an according DNS configuration.

### IzPack@Sonatype

Sonatype provides the service of deploying snapshots and releases to the Maven Central repository.

For us being an open source project with the appropriate license, Sonatype offers a staging repository for our deployed binaries along with using the Nexus Professional repository service for free. 
See our wiki page Deploying IzPack for more information how this is set up and used.

### IzPack@Atlassian

We have been set up a new JIRA and Confluence cloud instance kindly provided by Atlassian we received an on-demand open source license for. The central address is https://izpack.atlassian.net/.

Although the number of registered users is limited everyone is currently allowed to sign up.

Important  |
---------  |
There is a drawback after importing the legacy issues from Codehaus JIRA: There are the same user accounts used for JIRA and Confluence, each of both can be enabled separately for each user. The former users have been recreated along with the issues they participated in, but just their synonyms. There are auto-generated user names and the mail addresses got lost. If you have been alread signed up to Codehaus, before signing up again try to find your user name in some older issues and ask by mail to (re)set your user name and mail address. Please tell me the full name shown in the issues, your current e-mail address and at least one Codehaus JIRA issue you participated in for security reasons. This will save work of moving references to issues and cleaning up duplicate user accounts. After that you will be probably able to reset the password of this migrated account and log in again. If you feel this is a problem for you just sign up straight away and from time to time I will try to clean up the user accounts manually. Former Confluence-only users must re-register in each case.  |

You can sign up to JIRA and Confluence with one and the same user account. It is your choice which one of both services you want use.

#### Issue tracker - JIRA

IzPack issues will continue to be tracked to JIRA. The central address is https://izpack.atlassian.net/.

By the way, the former Codehaus administrators kindly offered a HTTP redirection from the old Codehaus issues to the new address at Codehaus itself in case there are still old links. This has been activated for all former IZPACK issues and their issues IDs have been kept.

The Github issue tracking has been deactivated to not confuse anyone and because it is just too plain at the moment. There is no real advantage for us at the moment in using Github issues.

#### Confluence - Wiki

The IzPack Wiki has been reimported and left on Confluence. It seems to be the more comfortable choice and better user experience compared to the Github Wiki at the moment.

The central entry point to IzPack Confluence is at https://izpack.atlassian.net/wiki, or check the [IzPack documentation](https://izpack.atlassian.net/wiki/display/IZPACK/) directly. The content is considered to be up to date for Izpack 5, feel free to help us directly improving the documentation.

### IzPack@Google

#### Google Groups - mailing lists

Regarding the mailing lists we ended up in reusing the existing mailing lists at Google Groups. There is a main reason - we kept all the old messages and subscribers from the Codehaus mailing lists mirrored there automatically and can continue to use it slightly.

Please note in advance that these are real mailing lists and you do not need necessarily an Google account at all to join them, see [this support notice](https://support.google.com/groups/answer/46438).

The existing subscribers have been left and don't probably have to re-subscribe. All existing lists have been left, just the mail addresses change.

In particular, there are activated the following lists:
- [izpack-announce@googlegroups.com](mailto:izpack-announce@googlegroups.com)<br>
[Read more about the izpack-announce group](https://groups.google.com/forum/#!aboutgroup/izpack-announce).<br>
Do no longer use the former list ~~announce@izpack.codehaus.org~~!<br>
If you are not a member you can subscribe by sending an e-mail to:
[izpack-announce+subscribe@googlegroups.com](mailto:izpack-announce+subscribe@googlegroups.com).
- [izpack-user@googlegroups.com](mailto:izpack-user@googlegroups.com)<br>
[Read more about the izpack-user group](https://groups.google.com/forum/#!aboutgroup/izpack-user).<br>
Do no longer use the former list ~~user@izpack.codehaus.org~~!<br>
If you are not a member you can subscribe by sending an e-mail to:
[izpack-user+subscribe@googlegroups.com](mailto:izpack-user+subscribe@googlegroups.com).
- [izpack-dev@googlegroups.com](mailto:izpack-dev@googlegroups.com)<br>
[Read more about the izpack-dev group](https://groups.google.com/forum/#!aboutgroup/izpack-dev).<br>
Do no longer use the former list ~~dev@izpack.codehaus.org~~!<br>
If you are not a member you can subscribe by sending an e-mail to:
[izpack-dev+subscribe@googlegroups.com](mailto:izpack-dev+subscribe@googlegroups.com).

If you are not a member of one of the above groups of your interest you may subscribe to them with or without being logged on as Google user on the web interface or by mail. For more help on Google Groups [visit the according help center](https://support.google.com/groups).

Be invited to join us again.

### IzPack@Twitter

IzPack has a [Twitter account](https://twitter.com/izpack). We will forward blog posts there and it is open for your tweets for registered Twitter users. Or just follow us to get the latest news.
