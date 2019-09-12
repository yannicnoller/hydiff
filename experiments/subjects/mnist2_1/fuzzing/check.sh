# chmod +x check.sh
trap "exit" INT

if [ -z "$1" ] # check for empty string
then
  echo "Define input folder!"
  exit 1
fi

for entry in $1/*
do
  echo "$entry"
  java -cp bin FuzzDriver "$entry"
done
