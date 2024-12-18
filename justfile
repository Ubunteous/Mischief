default:
	just --list


shadow-init:
	npx create-cljs-project app

node-repl:
	npx shadow-cljs node-repl

browser-repl:
	npx shadow-cljs browser-repl

reload-direnv:
	nix-direnv-reload

shadow-watch-frontend:
	npx shadow-cljs watch frontend

run:
	clj -M -m dashboard.main

format_check:
    clojure -M:format -m cljfmt.main check src dev test

format:
    clojure -M:format -m cljfmt.main fix src dev test

# repl:
#    clojure -M:nREPL -m nrepl.cmdline
#    clojure -M:dev -m nrepl.cmdline

test:
	clojure -M:dev -m kaocha.runner

lint:
	clojure -M:lint -m clj-kondo.main --lint .
	
outdated:
    clojure -Sdeps '{:deps {com.github.liquidz/antq {:mvn/version "RELEASE"}}}' -M -m antq.core
