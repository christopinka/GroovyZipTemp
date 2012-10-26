import groovy.json.*

// Must have arg
if (args.size() != 1){
	println "We'll need a valid US zip code. Thanks."
        return
}

def zipCode = args.getAt(0)
def validZipPattern = /\d{5}/

//Arg matches pattern
if(!zipCode.matches(validZipPattern)){
	println 'Double check your zip code. Only 5 digit zip codes are allowed. Sorry.'
	return
}
/**
def urlBaseStem = ''
def params = [chs:'250x100', chd:'t:60,40', cht:'p3', chl:'Hello|World']

def url = base + params.collect { k,v -> "$k=$v" }.join('&')
>>>>>>>>def url = base + params.collect { it }.join('&')


**/
//todo. 404 or other http response? When can response be null?
def geoCodeResponse = new URL("http://maps.googleapis.com/maps/api/geocode/json?address=${zipCode}&sensor=true").text
def locations = new JsonSlurper().parseText(geoCodeResponse)

//println locations

//Response not null, check status field
//Todo locations could be null and  status could be null or not there, wrap accordingly
if(locations.status != 'OK'){
	println "Doouble check your zip code. This zip code wasn't found. Sorry."
	return
}


//todo. results, address_components could break, null or not found, wrap accordingly
/**
println 'fa: ' + locations.results.formatted_address
println 'type: ' + locations.results.address_components.types.findAll()

def t = locations.results.find{it.formatted_address.equals('Columbia, MO 65203, USA')}
def location = locations.results.find{it.address_components.types.contains('country')}
println location
**/

def country
	locations.results.address_components.each{addressComponent ->
		addressComponent.each{ 
			if (it.types.contains('country') && it.long_name.equals('United States')){
				country = it.long_name	
			}
		}	
	}

// Country other than US or US not found
if(!country.equals('United States')){
	println 'The region for zip code ${zipCode} is outside of the United States.'
	return
}

//def foo 
//foo = ['bar', 'baz']
//assert "bar" == foo?.find{true}
	//Otherwise take the first US result in the list of possibly ambiguous results and avoid out of bounds if empty

def address = locations?.results?.formatted_address?.find{true}

	//Was address found?
if(address == null){
	println "No address information was found for zip code ${zipCode}."
	return
}

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
	The current temperature for ${address} is  
		${weather?.current_observation?.temp_f} degrees fahrenheit. 
		Please, enjoy your day.
 """
