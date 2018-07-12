#!/usr/bin/env bash


mkdir -p test_out

for dataset in "bangladesh" "chile" "china_mainland"
do
	for scaleM in "5"
	do
    	echo "Generalisation for "$dataset" - 1:"$scaleM"000000"
		java -jar RegionSimplify.jar -i test_data/$dataset.shp -o test_out/$dataset-$scaleMM.shp -s $scaleM000000
	done
done
