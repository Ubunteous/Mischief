default:
	just --list

[group('Format')]
format_check:
    clojure -M:format -m cljfmt.main check src dev test

[group('Format')]
format:
    clojure -M:format -m cljfmt.main fix src dev test

[group('Run')]
test:
	clojure -M:dev -m kaocha.runner

[group('Format')]
lint:
	clojure -M:lint -m clj-kondo.main --lint .

[group('Run')]
run:
	clj -M -m mischief.main

[group('Build')]
build-clean:
	clj -T:build clean

[group('Build')]
build-jar:
	clj -T:build jar

[group('Build')]
build-uberjar:
	clj -T:build uber

[group('Run')]
run-uberjar:
	java -jar target/mischief-*-standalone.jar

[group('Container')]
build-container:
	podman build -t mischief .

[group('Container')]
show-containers:
	podman ps --all

[group('Container')]
show-images:
	podman images | grep mischief

[group('Container')]
remove-images:
	podman rmi --force --all

[group('Container')]
run-container:
	podman run -p 9999:9999/tcp localhost/mischief

[group('Container')]
stop-container:
	podman stop -all

[group('Misc')]
deploy: build-uberjar build-container run-container
	build-uberjar && build-container && run-container

[group('Misc')]
reload-direnv:
	nix-direnv-reload

[group('Misc')]
outdated-deps:
    clojure -Sdeps '{:deps {com.github.liquidz/antq {:mvn/version "RELEASE"}}}' -M -m antq.core
