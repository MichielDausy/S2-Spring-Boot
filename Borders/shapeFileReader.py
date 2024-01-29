import geopandas as gpd

# Read the shapefile
shape = gpd.read_file('my_shapefile.shp')

# Plot the shapefile
shape.plot()