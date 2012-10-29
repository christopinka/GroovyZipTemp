import groovy.json.*

if (args.size() != 1){
	println "We'll need a valid US zip code. Thanks."
        return
}

def zipCode = args.getAt(0)
def validZipPattern = /\d{5}/

if(!zipCode.matches(validZipPattern)){
	println 'Double check your zip code. Only 5 digit zip codes are allowed. Sorry.'
	return
}

def urlBaseStem = 'http://maps.googleapis.com/maps/api/geocode/json?'
def params = [address:"${ zipCode }", sensor:'true']
def url = urlBaseStem + params.collect { k,v -> "$k=${ URLEncoder.encode(v) }" }.join('&')

//todo. 404 or other http response? When can response be null?
def geoCodeResponse = new URL(url).text
def locations = new JsonSlurper().parseText(geoCodeResponse)

//println locations
if(locations.status != 'OK'){
	println "Doouble check your zip code. This zip code wasn't found. Sorry."
	return
}

def location = locations?.results?.find{ it.address_components?.long_name?.find{ country-> country.equals('United States')}\
	 && it.address_components?.types.find{ types-> types.contains('country') } }

def wunderGroundConditionsRequest = "http://api.wunderground.com/api/3df1783a4eab5c53/geolookup/conditions/q/${zipCode}.json"
def wunderGroundConditionsResponse = new URL(wunderGroundConditionsRequest).text
def weather = new JsonSlurper().parseText(wunderGroundConditionsResponse)

// Cover the rest of the error responses
//println weather
if(weather.response.error){
  	println "${weather.response.error.description} of zip code ${zipCode}."
	return
}

println """\
	The current temperature for ${location?.formatted_address?:zipCode} is  
		${weather?.current_observation?.temp_f} degrees fahrenheit. 
		Please, enjoy your day.
 """
