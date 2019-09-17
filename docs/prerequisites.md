# Prerequisites

## Codebase

The ORCID-rest-api changes have been released as a patch for DSpace as this allows for the easiest installation process of the incremental codebase.

**__Important note__**: Below, we will explain how to apply the patch to your existing installation. This will affect your source code. Before applying a patch, it is always recommended to create a backup of your DSpace source code.

In order to apply the patch, you will need to locate the **DSpace source code** on your server. That source code directory should look similar to the following structure:

```
[dspace-src]
  - dspace
  - ...
  - LICENSE
  - NOTICE
  - README 
```

For every release of DSpace, generally two release packages are available. One package has "src" in its name and the other one doesn't. The difference between the two is that the release labelled "src" contains all of the DSpace source code, whereas the other release retrieves precompiled packages for specific DSpace artifacts from maven central. **The ORCID-rest-api patches were designed to work on both "src" and other release packages of DSpace**.

To be able to install the patch, you will need the following prerequisites:

* A DSpace version that is viable for the patch installation and contains the [ORCID API 2.0 functionality integration](https://jira.duraspace.org/browse/DS-3447). Viable DSpace versions are listed below. Prior versions are not elibigle for the patch application without additional porting of the ORCIDv2 code.
  * DSpace 5.9+
  * DSpace 6.3+
* Maven 3.2.2 or greater.
* A machine that has Git installed

## Download patch

Atmire's modifications to a standard DSpace for the ORCID-rest-api are tracked on Github. The newest patch can therefore be generated from git.

| DSpace | Patch                                                                       |
| ------ | --------------------------------------------------------------------------- |
| 5.x    | [Download](https://github.com/atmire/ORCID-rest-api/compare/dspace_5x…stable_5x.diff) |
| 6.x    | [Download](https://github.com/atmire/ORCID-rest-api/compare/dspace_6x…stable_6x.diff) |


Save this file under a meaningful name. It will later be referred to as `<patch>`.
