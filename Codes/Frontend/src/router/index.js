import { createRouter, createWebHistory } from 'vue-router';
import LoginPage from '@/views/Login.vue';
import MainLayout from '@/views/MainLayout.vue';
import DeviceCenter from '@/views/DeviceCenter.vue';
import MonitorCenter from '@/views/MonitorCenter.vue';
import DataSimulation from '@/views/DataSimulation.vue';
import AlertSystem from '@/views/AlertSystem.vue';
import ReportSystem from '@/views/ReportSystem.vue';

const routes = [
  {
    path: '/',
    name: 'LoginPage',
    component: LoginPage
  },
  {
    path: '/main',
    name: 'MainLayout',
    component: MainLayout,
    children: [
      {
        path: 'device-center',
        name: 'DeviceCenter',
        component: DeviceCenter,
        meta: { navId: 'devices' }
      },
      {
        path: 'monitor/:deviceId',
        name: 'MonitorCenter',
        component: MonitorCenter,
        props: true,
        meta: { navId: 'monitor' }
      },
      {
        path: 'data-simulation',
        name: 'DataSimulation',
        component: DataSimulation,
        meta: { navId: 'simulation' }
      },
      {
        path: 'alert-system',
        name: 'AlertSystem',
        component: AlertSystem,
        meta: { navId: 'alert' }
      },
      {
        path: 'report-system',
        name: 'ReportSystem',
        component: ReportSystem,
        meta: { navId: 'report' }
      }
    ]
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

export default router;