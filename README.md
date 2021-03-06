# membrane-re-frame-example

A proof of concept to demonstrate using re-frame for desktop and terminal apps.

The code is largely derived from https://github.com/day8/re-frame/tree/master/examples/todomvc. Except for some browser specific code, the subs, db, and events are the same.

Both the desktop and the terminal app fully share subs, db, and events. Each has their own view implementation.

# To Run

- membrane: `make membrane`
- cljfx: `make cljfx`

## Gene notes on Membrane implementation

- reloading repl namespace updates app
- subscription changes requires http://day8.github.io/re-frame/api-re-frame.core/#clear-subscription-cache
- focus and unfocus event
- wrap-on: events live around the widget
- use test-scrollview to get minimally managed scrollbars around window
    - similarly, you need to handle line wraps yourself
- this is a wild architecture: state is done by values!
    - uses lenses
    - as proof, check out @memframe/text-boxes
    - here is unfocus: (swap! memframe/text-boxes assoc ::focus nil)
    
- when updating subscriptions, call `(re-frame.subs/clear-subscription-cache!)`


## Screenshots

### Desktop
![desktop](desktop-demo.gif?raw=true)

### Terminal
![terminal example](term-demo.gif?raw=true)


## Usage

### Desktop
`$ lein run -m membrane-re-frame-example.views`
### Terminal
`$ lein run -m membrane-re-frame-example.term-view`

re-frame+membrane can be compiled using graalvm. To compile your terminal app using graalvm:

1. [Download and install graalvm](https://github.com/BrunoBonacci/graalvm-clojure/blob/master/doc/clojure-graalvm-native-binary.md#step1---download-and-install-graalvm)
2. Run `lein native`
3. Run the terminal app `./target/membrane-re-frame-example`

## License

Copyright © 2020 FIXME

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
