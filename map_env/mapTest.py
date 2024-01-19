import folium
import requests
from shapely import wkt

# Replace the URL with your actual API endpoint
api_url = 'http://localhost:8081/api/tracks/map?trackId=1'

# Make a GET request to the API endpoint
response = requests.get(api_url)

# Check if the request was successful (status code 200)
if response.status_code == 200:
    # Parse the WKT string into a Shapely geometry object
    wkt_geometry = response.text.strip()
    geometry = wkt.loads(wkt_geometry)

    # Extract coordinates from the Shapely geometry object
    coordinates = list(geometry.coords)
    print(coordinates)

    # Swap the order of coordinates (longitude, latitude) to (latitude, longitude)
    coordinates = [(coord[1], coord[0]) for coord in coordinates]
    print(coordinates)

    # Create a map centered at the first coordinate
    m = folium.Map(location=[coordinates[0][0], coordinates[0][1]], zoom_start=12)

    # Add a line to the map with the coordinates
    folium.PolyLine(coordinates, color="red", weight=2.5, opacity=1).add_to(m)

    # Save the map to an HTML file
    m.save('map.html')
else:
    # Print an error message if the request was not successful
    print(f"Error: {response.status_code}")
