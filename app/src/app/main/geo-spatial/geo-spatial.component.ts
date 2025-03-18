import { Component, ViewChild } from '@angular/core';
import { GeoSpatialService } from './geo-spatial.service';
import { MapAdvancedMarker, MapInfoWindow } from '@angular/google-maps';

@Component({
  selector: 'app-geo-spatial',
  templateUrl: './geo-spatial.component.html',
  styleUrl: './geo-spatial.component.scss',
  standalone: false
})
export class GeoSpatialComponent {
  keys = Object.keys;
  start: any;
  end: any;

  routes: any[] = [];
  stops: any[] = [];
  suggestedRoutes: any[] = [];
  suggestedRouteStops: any = {};

  mapOptions: google.maps.MapOptions = {
    mapId: "map",
    center: { lat: 22.3193, lng: 114.1694 },
    zoom: 12,
    clickableIcons: false
  };

  @ViewChild(MapInfoWindow) infoWindow!: MapInfoWindow;

  selectedStop: any;

  constructor(private service: GeoSpatialService) {

  }
  addPoint(event: google.maps.MapMouseEvent) {
    if (this.start && this.end) {
      this.clear();
    }
    if (!this.start && !this.end) {
      this.clearOverlays();
      this.findStopsNearby(event.latLng);
      this.findRoutesNearby(event.latLng);
      this.start = event.latLng;
    }
    else if (this.start && !this.end) {
      this.end = event.latLng;
      this.search();
    }
  }
  findStopsNearby(latLng: google.maps.LatLng | null) {
    if (latLng)
      this.service.findStopsNearby({
        lat: latLng.lat(),
        lng: latLng.lng()
      }).subscribe({
        next: (result: any) => {
          this.stops = result;
        },
        error: (err: any) => {
        }
      });
  }
  findRoutesNearby(latLng: google.maps.LatLng | null) {
    if (latLng)
      this.service.findRoutesNearby({
        lat: latLng.lat(),
        lng: latLng.lng()
      }).subscribe({
        next: (result: any) => {
          this.routes = result;
        },
        error: (err: any) => {
        }
      });
  }
  search() {
    this.service.search({
      start: [this.start.lng(), this.start.lat()],
      end: [this.end.lng(), this.end.lat()]
    }).subscribe({
      next: (result: any) => {
        this.clearOverlays();
        this.suggestedRoutes = result;
        let stops: any = {};
        this.suggestedRoutes.forEach(r => {
          r.legs.forEach((l: any, i: number) => {
            const startStop = l.stops[l.startIndex];
            const endStop = l.stops[l.endIndex];
            if (i == 0 && startStop) {
              if (!stops[startStop.id]) {
                stops[startStop.id] = { start: true, details: startStop, routes: [] };
              }
              stops[startStop.id].start = true;
              stops[startStop.id].routes.push(l.route + " " + l.serviceType);
            }
            if (i == r.legs.length - 1 && endStop) {
              if (!stops[endStop.id]) {
                stops[endStop.id] = { end: true, details: endStop, routes: [] };
              }
              stops[endStop.id].end = true;
              stops[endStop.id].routes.push(l.route + " " + l.serviceType);
            }
          });

          if (r.transferStops) {
            r.transferStops.forEach((s: any, i: number) => {
              if (!stops[s.id]) {
                stops[s.id] = { transfer: true, details: s, routes: [] };
              }
              if (r.legs[i]) {
                stops[s.id].transfer = true;
                stops[s.id].routes.push(r.legs.map((l: any) => l.route + " " + l.serviceType).join(">"));
              }
            });
          }
        });
        this.suggestedRouteStops = stops;
      },
      error: (err: any) => {
      }
    });
  }

  clear() {
    this.start = null;
    this.end = null;
  }
  clearOverlays() {
    this.stops.length = 0;
    this.routes.length = 0;
    this.suggestedRoutes.length = 0;
    this.suggestedRouteStops = {};
  }
  openInfoWindow(marker: MapAdvancedMarker, s: any) {
    this.selectedStop = s;
    this.infoWindow.open(marker);
  }
  openSuggestedRouteInfoWindow(marker: MapAdvancedMarker, k: any) {
    this.selectedStop = this.suggestedRouteStops[k];
    this.infoWindow.open(marker);
  }
  getMarkerOption(k: string): google.maps.marker.AdvancedMarkerElementOptions {
    let color = "#";
    color += (this.suggestedRouteStops[k].start) ? "FF" : "00";
    color += (this.suggestedRouteStops[k].transfer) ? "FF" : "00";
    color += (this.suggestedRouteStops[k].end) ? "FF" : "00";
    return {
      content: new google.maps.marker.PinElement({
        background: (this.suggestedRouteStops[k].routes.length == 0) ? "#FBBC04" : color,
      }).element,
      gmpClickable: true,
    };
  }
  getPath(r: any) {
    return r.stops.map((s: any) => {
      return { lat: s.location.position.values[1], lng: s.location.position.values[0] }
    });


  }
  getPolylineOption(color: string) {
    return {
      strokeColor: color,
      strokeOpacity: 0.2,
      strokeWeight: 1,
    }
  }
}
