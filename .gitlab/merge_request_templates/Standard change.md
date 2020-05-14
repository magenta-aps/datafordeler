## What does this MR do?
<!--
Briefly describe what this MR is about.
Examples:
 Adds new document type: MyNewDocumentType
 Fixes js error in <some functionality>
-->


## Code quality checklist

- [ ] I have added unit tests or made a conscious decision not to
- [ ] Code has been reformatted according to chosen standard
- [ ] The code is documented where relevant
- [ ] The branch has been rebased on / merged with top of the latest version of its target branch
- [ ] All commits reference the ticket id (eg. `[#12345] Implement featureX in Y`)
- [ ] All commits on this MR are atomic/logical (via `rebase -i develop`), or: the MR is set to be squashed into a single commit on merge

## Pre-review instructions

* Make sure that the ticket has `WIP:` in the start of the title unless it is ready for deployment
* Ensure that the title of this MR contains the relevant ticket no., formatted like `[#12345]` or `#12345`
* Add deployment notes on the corresponding Redmine ticket if relevant
* `@assign` this MR to your choice of reviewer
* Set the corresponding Redmine ticket to `Needs review`, assign it to the reviewer and add a link to this page to the `Merge request` field.
* Provide instructions on how to test the feature (which environment, example URLs etc.)

---

## Review checklist

- [ ] The code is understandable, well-structured and sufficiently documented
- [ ] I would be able to deploy this feature and verify that it's working without further input from the author
- [ ] I have run relevant unit-tests and made sure they succeeded
- [ ] I have checked out the code and tested locally, tested the feature on the testing server or thorougly vetted the code

If this MR contains database changes, please do a dry run of a potential deployment in your local environment.

## Merge instructions

* If the MR is marked as work-in-progres (it has `WIP:` in the start of the title):
** Approve the MR
** Unassign yourself
** Assign the MR to the original author or the release manager
** Add a note in redmine saying the MR was approved, change redmine ticket to `In progress`
   and assign the redmine ticket to the same person you assigned the MR to. 
* If the MR is not work-in-progress:
** Merge it.
** Set the corresponding Redmine ticket to `Done` and unassign yourself
* In both cases:
** If set up for squashing, make sure the squash commit message makes sense
