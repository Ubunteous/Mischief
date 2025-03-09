default:
	just --list

format_check:
    clojure -M:format -m cljfmt.main check src dev test

format:
    clojure -M:format -m cljfmt.main fix src dev test

test:
	clojure -M:dev -m kaocha.runner

lint:
	clojure -M:lint -m clj-kondo.main --lint .

run:
	clj -M -m mischief.main

build-clean:
	clj -T:build clean

build-jar:
	clj -T:build jar

build-uber:
	clj -T:build uber

run-uberjar:
	java -jar target/mischief-*-standalone.jar

reload-direnv:
	nix-direnv-reload
