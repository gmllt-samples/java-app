PORT ?= 8080

run:
	@echo "Compiling and running on port $(PORT)..."
	@mkdir -p out
	@javac -d out src/app/*.java
	@PORT=$(PORT) java -cp out app.Main

clean:
	rm -rf out

.PHONY: run clean
