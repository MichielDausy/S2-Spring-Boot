import requests
import folium
from shapely import wkt
from shapely.geometry import MultiPoint, MultiLineString, MultiPolygon
import re

# Replace the URL with your actual API endpoint
api_url = 'http://localhost:8081/api/anomalies/map'
api_url_tracks = 'http://localhost:8081/api/tracks/all'
api_url_country = 'http://localhost:8081/api/countries/all'

# Make a GET request to the API endpoint
response = requests.get(api_url)
response_tracks = requests.get(api_url_tracks)
response_countries = requests.get(api_url_country)

# Check if the request was successful (status code 200)
if response.status_code == 200:
    # Get the list of Points
    point_wkts = re.findall(r'POINT\(([^)]+)\)', response.text)

    # Get the list of LineStrings
    linestring_wkts = re.findall(r'\"(LINESTRING\([^)]+\))\"', response_tracks.text)

    # Get the list of Polygons
    polygon_wkts = re.findall(r'\"(POLYGON\([^)]+\))\"', response_countries.text)

    # Convert each WKT to a Shapely Point and add it to a list
    points = [wkt.loads(f"POINT({point})") for point in point_wkts]

    # Convert each WKT to a Shapely LineString and add it to a list
    linestrings = [wkt.loads(wkt_text) for wkt_text in linestring_wkts]

    # Convert WKT to a Shapely Polygon and add it to a list
    polygons = [wkt.loads(wkt_text) for wkt_text in polygon_wkts]

    # Create a Shapely MultiPoint from the list of Points
    multipoint = MultiPoint(points)

    # Create a Shapely MultiLineString from the list of LineStrings
    multiline_string = MultiLineString(linestrings)

    # Create a Shapely MultiPolygon from the list of Polygons
    multipolygon = MultiPolygon(polygons)

    # Calculate the center of the bounding box
    center_lat = (multipoint.bounds[1] + multipoint.bounds[3]) / 2
    center_lon = (multipoint.bounds[0] + multipoint.bounds[2]) / 2

    # Create a folium map centered at the bounding box center
    mymap = folium.Map(location=[center_lat, center_lon], zoom_start=14)

    # Convert Shapely MultiPoint to GeoJSON format
    multipoint_geojson = folium.GeoJson(data=multipoint.__geo_interface__)

    # Define a style function to set the color of the GeoJson layer
    def style_function(feature):
        return {
            'color': 'red',  # Change the color to your desired color
            'weight': 2,
            'opacity': 1,
        }

    # Convert Shapely MultiLineString to GeoJSON format with style
    multiline_geojson = folium.GeoJson(
        data=multiline_string.__geo_interface__,
        style_function=style_function
    )

    # Convert Shapely MultiPolygon to GeoJSON format with style
    multipolygon_geojson = folium.GeoJson(
        data=multipolygon.__geo_interface__,
        style_function=style_function
    )

    # Add GeoJSON to the map
    multipoint_geojson.add_to(mymap)

    # Add GeoJSON to the map
    multiline_geojson.add_to(mymap)

    # Add GeoJSON to the map
    multipolygon_geojson.add_to(mymap)

    # Save the map to an HTML file
    mymap.save("map.html")
else:
    # Print an error message if the request was not successful
    print(f"Error: {response.status_code}")
