sbt frontend/fullOptJS
yarn exec vite -- build
cp dist/index.html dist/200.html
surge ./dist 'scala-school.surge.sh'
