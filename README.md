# AppWeather

**AppWeather** is a weather forecasting application built for Android using Java. It provides users with real-time weather updates, forecasts, and a radar map for tracking weather conditions in various locations, with a focus on cities in Vietnam.

## Features
- **Real-Time Weather Updates**: Displays current temperature, weather conditions (e.g., rain, clouds), "feels like" temperature, wind speed, and humidity.
- **Hourly and 5-Day Forecast**: View weather predictions for the current day and the next five days.
- **Weather Radar Map**: Visualize weather patterns with a radar map, showing precipitation and weather intensity in the user's current location.
- **Location-Based Weather**: Automatically detects the user's current location or allows manual city searches (e.g., Ha Dong, Haiphong, Hanoi).
- **Multi-City Weather Comparison**: Check weather conditions in multiple cities across Vietnam, such as Hanoi, Ho Chi Minh City, and Da Nang.
- **User-Friendly Interface**: Clean and intuitive design with weather icons and a gradient background for better readability.

## Screenshots
1. **Radar Map Screen**  
   Displays the user's current location on a map with weather details (e.g., 30.0°C, heavy intensity rain, 80% humidity). Nearby landmarks like "Aeon Mall Ha Dong" and "Cong Vien Thien Duong Bao Son" are visible.  
   ![Radar Map](screenshots/radar_map.jpg)

2. **Ha Dong Weather Screen**  
   Shows the weather in Ha Dong, VN, with current conditions (30°C, rain, 37°C "feels like", 1m/s wind speed, 80% humidity) and hourly forecasts for the day.  
   ![Ha Dong Weather](screenshots/ha_dong_weather.jpg)

3. **Haiphong Weather Screen**  
   Displays the weather in Haiphong, VN, with current conditions (30°C, clouds, 37°C "feels like", 5m/s wind speed, 79% humidity) and hourly forecasts.  
   ![Haiphong Weather](screenshots/haiphong_weather.jpg)

4. **5-Day Forecast Screen**  
   Provides a 5-day weather forecast for Ha Dong, VN, showing daily conditions (e.g., rain, clouds) and high/low temperatures.  
   ![5-Day Forecast](screenshots/5_day_forecast.jpg)

5. **Multi-City Weather Screen**  
   Lists weather conditions for multiple cities in Vietnam, such as Hanoi (33.9°C, overcast clouds), Ho Chi Minh City (31.6°C, few clouds), and the user's current location (30.0°C, heavy intensity rain).  
   ![Multi-City Weather](screenshots/multi_city_weather.jpg)

6. **Detailed Radar Map Screen**  
   A detailed view of the radar map with a different map style, showing the user's location and weather intensity.  
   ![Detailed Radar Map](screenshots/detailed_radar_map.jpg)

7. **Empty Weather Screen**  
   A placeholder screen with fields for "Feels like", "Wind speed", and "Humidity", ready to display weather data.  
   ![Empty Weather Screen](screenshots/empty_weather_screen.jpg)

## Installation
1. **Prerequisites**  
   - Android Studio (latest version recommended)
   - Java Development Kit (JDK) 8 or higher
   - An Android device or emulator running Android 5.0 (Lollipop) or above

2. **Steps**  
   - Clone the repository:
  - Open the project in Android Studio.
  - Sync the project with Gradle.
  - Build and run the app on an emulator or physical device.

## Usage
- Launch the app on your Android device.
- Allow location permissions to automatically detect your current location, or search for a city manually (e.g., "Hanoi").
- View real-time weather data, hourly forecasts, and a 5-day forecast.
- Use the radar map to track precipitation and weather patterns in your area.

## Technologies Used
- **Java**: Core programming language for Android development.
- **Android SDK**: For building the app's UI and functionalities.
- **Google Maps API**: For the radar map feature (assumed based on map style).
- **Weather API**: For fetching real-time weather data (e.g., OpenWeatherMap or similar, assumed).

## Contributing
Contributions are welcome! Please fork the repository, create a new branch, and submit a pull request with your changes.

## License
This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

## Contact
For questions or feedback, reach out to [longcanh2k3@gmail.com](mailto:longcanh2k3@gmail.com).
