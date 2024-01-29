import geopandas as gpd
import matplotlib.pyplot as plt
import os
from pyproj import Transformer

# Read the shapefile
shape = gpd.read_file('C:/Users/Michiel/Dropbox/pc/Desktop/Thomas_More/2023 - 2024/Project 4.0/S2-Spring-Boot/be_1km.shp')

# Create a transformer
transformer = Transformer.from_crs(shape.crs, 'EPSG:4326', always_xy=True)

# Get the boundary of the shapefile
boundary = shape.boundary

# Specify the directory where you want to create the text file
directory = 'C:/Users/Michiel/Dropbox/pc/Desktop/Thomas_More/2023 - 2024/Project 4.0/S2-Spring-Boot/'

# Create the full file path
file_path = os.path.join(directory, 'coordinates.txt')

# Open the text file in write mode
with open(file_path, 'w') as f:
     # Loop through each feature in the boundary GeoSeries
    for feature in boundary:
        # Convert the CoordinateSequence to a list
        coords = list(feature.coords)
        # Transform each coordinate
        transformed_coords = [transformer.transform(x, y) for x, y in coords]
        # Write the transformed coordinates to the text file
        f.write(str(transformed_coords) + '\n')

# Plot the shapefile
shape.plot()
plt.show()
