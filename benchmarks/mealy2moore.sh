for filename in mealy/**/*.dot
do    stm convert mealy2moore ${filename} moore/${filename}
done