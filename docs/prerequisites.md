# Prerequisites

## Codebase

The expanded-ORCID-support changes have been released as a patch for DSpace as this allows for the easiest installation process of the incremental codebase.

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

For every release of DSpace, generally two release packages are available. One package has "src" in its name and the other one doesn't. The difference between the two is that the release labelled "src" contains all of the DSpace source code, whereas the other release retrieves precompiled packages for specific DSpace artifacts from maven central. **The expanded-ORCID-support patches were designed to work on both "src" and other release packages of DSpace**.

To be able to install the patch, you will need the following prerequisites:

* A DSpace version that is viable for the patch installation and contains the [ORCID API 2.0 functionality integration](https://jira.duraspace.org/browse/DS-3447). Viable DSpace versions are listed below. Prior versions are not eligible for the patch application without additional porting of the ORCIDv2 code.
  * DSpace 5.9+
  * DSpace 6.3+
* Maven 3.2.2 or greater.
* A machine that has Git installed

## Download patch

Atmire's modifications to a standard DSpace for the expanded-ORCID-support are tracked on Github. The newest patch can therefore be generated from git.

| DSpace | Patch                                                                       |
| ------ | --------------------------------------------------------------------------- |
| 5.x    | [Download](https://github.com/atmire/expanded-ORCID-support/blob/master/patches/dspace_5x.patch) |
| 6.x    | [Download](https://github.com/atmire/expanded-ORCID-support/blob/master/patches/dspace_6x.patch) |



Save this file under a meaningful name. It will later be referred to as `<patch>`.

For those who are so inclined, you can also create the patch yourself based on the difference between the stable_5x and dspace_5x which is the branch with the orcid related code, and the unaltered Dspace branches.
An example of the commands to use, for the DSpace5 version (Similar for DSpace6):
* Checkout the stable_5x branch.
* Create the path file similar to the following: 
    * git diff --full-index --binary dspace_5x >> dspace_5x.patch 
    * When creating the patch, please be advised that the binaries need to be included as well, so that the images etc are also exported correctly.
* The patch should now be basically the same one as the one present on the 'patches' branch
