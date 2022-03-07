# Overview

This repo hosts documentation using GitHub Pages (available [here](https://alvearie.github.io/quality-measure-and-cohort-service/)).
Documentation is rendered on the fly using [docsify](https://docsify.js.org/#/). Markdown files on the `main` branch in the
`docs/` folder will be available in GitHub Pages.

## Adding Pages
New pages can be added by including additional markdown files under the `docs/` folder (or subfolders located there).
Try to group pages for related topics in folders with meaningful names. Keep in mind that folder names will be visible in
the URL once the various pages are rendered.

This project is configured to include a sidebar when rendering the documentation. Links to new pages can be added in one of
the `_sidebar.md` files available in the repo. Alternatively, pages can be linked to directly from other pages.

## Search
Plaintext search is enabled when viewing the page on GitHub Pages. Default settings are in place, but there are some minor
configuration options described [here](https://docsify.js.org/#/plugins?id=full-text-search).

## Hosting Locally
If you would like to preview changes to the documentation locally, you can install docsify and use it to serve the current
version of the documentation you have checked out. Full details can be found in the [docsify quick start guide](https://docsify.js.org/#/quickstart?id=quick-start).

Basic Instructions:

* Install docsify: `npm i docsify-cli -g`
* Serve the docs locally (run from the root of this repo): `docsify serve docs/`

Installing docsify is not a requirement to contribute documentation. Doing so is strictly a way to verify that changes are
being rendered properly before merging back to `main`.
