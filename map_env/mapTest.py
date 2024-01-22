import requests
import folium
from shapely import wkt
from shapely.geometry import MultiPoint
import re

# Replace the URL with your actual API endpoint
api_url = 'http://localhost:8081/api/anomalies/map'

# Make a GET request to the API endpoint
response = requests.get(api_url)

# Check if the request was successful (status code 200)
if response.status_code == 200:
    # Get the list of Points
    point_wkts = re.findall(r'POINT\(([^)]+)\)', response.text)

    # Convert each WKT to a Shapely Point and add it to a list
    points = [wkt.loads(f"POINT({point})") for point in point_wkts]

    # Create a Shapely MultiPoint from the list of Points
    multipoint = MultiPoint(points)

    # Calculate the center of the bounding box
    center_lat = (multipoint.bounds[1] + multipoint.bounds[3]) / 2
    center_lon = (multipoint.bounds[0] + multipoint.bounds[2]) / 2

    # Create a folium map centered at the bounding box center
    mymap = folium.Map(location=[center_lat, center_lon], zoom_start=14)

    # Convert Shapely MultiPoint to GeoJSON format
    multipoint_geojson = folium.GeoJson(data=multipoint.__geo_interface__)

    # Add GeoJSON to the map
    multipoint_geojson.add_to(mymap)

    # Save the map to an HTML file
    mymap.save("map.html")
else:
    # Print an error message if the request was not successful
    print(f"Error: {response.status_code}")
