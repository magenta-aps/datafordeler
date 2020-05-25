for dir in */;
  do echo $dir;
  cd $dir
  mvn --batch-mode -T 2C -DskipTests clean install || exit 1
  cd ..
done
