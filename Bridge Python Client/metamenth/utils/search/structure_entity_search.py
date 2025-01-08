from typing import Dict
import sys
from datetime import datetime
from typing import Union
from typing import List
from metamenth.measure_instruments.sensor_data import SensorData
from metamenth.measure_instruments.trigger_history import TriggerHistory
from metamenth.measure_instruments.meter_measure import MeterMeasure
from metamenth.measure_instruments.weather_data import WeatherData
from metamenth.misc.validate import Validate
from py4j.java_gateway import JavaGateway


class StructureEntitySearch:
    """
    A visitor that entities in structures, e.g., meter, weather, stations, etc
    """

    def __init__(self):
        self._gateway = JavaGateway()

    def searchByUid(self, entity_list, uid):
        """
        search structures by unique identifiers
        :param entity_list: the list of entity to search for a particular entity
        :param uid: the unique identifiers
        :return:
        """
        return self.searchStructureEntity(entity_list, 'UID', uid)

    def searchByName(self, entity_list, name):
        """
        search structures by name
        :param entity_list: the list of entity to search for a particular entity
        :param name: name of the structure
        :return:
        """
        return self.searchStructureEntity(entity_list, 'Name', name)

    def searchByNumber(self, entity_list, number):
        """
        search structures by name
        :param entity_list: the list of entity to search for a particular entity
        :param number: number of the entity
        :return:
        """
        return self.searchStructureEntity(entity_list, 'Number', number)

    def search(self, entity_list, search_terms: Dict):
        """
        search entities based on attribute values
        :param entity_list: the list of entity to search for a particular entity
        :param search_terms: key value pair of attributes and their values
        :return:
        """
        results = self._gateway.jvm.java.util.ArrayList()
        for entity in entity_list:
            found = True
            try:
                if search_terms:
                    for attribute, value in search_terms.items():
                        # change attribute into getter
                        if not self._search_with_attribute(attribute, value, entity):
                            found = False
                    if found:
                        results.add(entity)
                else:
                    results.add(entity)
            except AttributeError as err:
                # TODO: log errors to file
                print(err, file=sys.stderr)
        return results

    def dateRangeSearch(self, entity_list: Union[List[SensorData], List[TriggerHistory], List[MeterMeasure],
    List[WeatherData]], from_timestamp: str, to_timestamp: str = None):
        """

        :param entity_list: a list of sensor, actuator or meter data
        :param from_timestamp: the start timestamp
        :param to_timestamp: the end timestamp
        :return:
        """
        if len(from_timestamp) == 10:  # Check if only date is provided
            from_timestamp += ' 00:00:00'  # Add default time of midnight

        if not to_timestamp:

            to_tp = datetime.now().replace(microsecond=0)
        else:
            if len(to_timestamp) == 10:  # Check if only date is provided
                to_timestamp += ' 23:59:59'
            to_tp = Validate.parseDate(to_timestamp)
        from_tp = Validate.parseDate(from_timestamp)
        filtered_data = self._gateway.jvm.java.util.ArrayList()

        for data in entity_list:
            dt = datetime.strptime(data.getTimeStamp(), "%Y-%m-%d %H:%M:%S")
            if from_tp <= dt <= to_tp:
                filtered_data.add(data)

        return filtered_data

    def searchStructureEntity(self, entity_list, search_field, search_value):
        """
        Search for structure floors, rooms, open spaces in a building
        :param entity_list: the list of entities to search
        :param search_field: the search field
        :param search_value: the search value
        :return:
        """
        for entity in entity_list:
            try:
                if self._search_with_attribute(search_field, search_value, entity):
                    return entity
            except AttributeError as err:
                # TODO: log errors to file
                print(err, file=sys.stderr)
        return None

    def _search_with_attribute(self, attr, value, entity) -> bool:
        attribute = 'get' + attr
        attr_getter = entity.__getattr__(attribute)
        return attr_getter() == value

    class Java:
        implements = ['ca.concordia.ngci.tools4cities.metamenth.interfaces.utils.search.IStructureEntitySearch']
