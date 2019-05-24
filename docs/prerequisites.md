# Prerequisites

## Codebase

The ORCID-rest-api changes have been released as a "patch" for DSpace as this allows for the easiest installation process of the incremental codebase.

**__Important note__**: Below, we will explain you how to apply the patch to your existing installation. This will affect your source code. Before applying a patch, it is **always** recommended to create backup of your DSpace source code.

In order to apply the patch, you will need to locate the **DSpace source code** on your server. That source code directory should look similar to the following structure:

```
[dspace-src]
  - dspace
  - ...
  - LICENSE
  - NOTICE
  - README 
```

For every release of DSpace, generally two release packages are available. One package has "src" in its name and the other one doesn't. The difference is that the release labelled "src" contains ALL of the DSpace source code, while the other release retrieves precompiled packages for specific DSpace artifacts from maven central. **The ORCID-rest-api patches were designed to work on both "src" and other release packages of DSpace**.

To be able to install the patch, you will need the following prerequisites:

* A running DSpace 5.x or 6.x instance.
* Maven 3.2.2 or greater.
* Git should be installed on the machine. The patch will be applied using several git commands as indicated in the next section.

## Download patch

Atmire's modifications to a standard DSpace for the ORCID-rest-api are tracked on Github. The newest patch can therefore be generated from git.

| DSpace | Patch                                                                       |
| ------ | --------------------------------------------------------------------------- |
| 5.x    | [Download](https://github.com/atmire/ORCID-rest-api/compare/dspace_5x…stable_5x.diff) |
| 6.x    | [Download](https://github.com/atmire/ORCID-rest-api/compare/dspace_6x…stable_6x.diff) |


Save this file under a meaningful name. It will later be referred to as `<patch>`.