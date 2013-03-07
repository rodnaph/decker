[![Build Status](https://secure.travis-ci.org/rodnaph/decker.png?branch=master)](http://travis-ci.org/rodnaph/decker)

# decker

Decker is a simple tool for copying data from one database to another.  It does
not support migrating schema.

## Usage with Leiningen

To run Decker first take a copy of _config.clj-sample_ and then fill in the
correct source and destination database information.  Then run:

```clojure
lein run ../path/to/config.clj
```

## Using as Library

To use Decker as a library just add it to your Leiningen project file, the you
can include it like this:

```clojure
(:require [decker.core :as decker])

; Copy all tables in a database
(decker/copy from to)

; Copy only the specified tables
(decker/copy-tables from to ["tbl1" "tbl2"])

; Copy a specific table
(decker/copy-table from to "table")
```

## Documentation

Documentation provided by Marginalia.

```
lein marg
```

Then browse _docs/_

## License

Distributed under the Eclipse Public License, the same as Clojure.

