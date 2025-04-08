"use client"

import withAuth from '@/utils/withAuth';
import React, { useState, useEffect, Suspense } from 'react'
import { User } from "lucide-react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import DashboardLayout from "@/components/dashboard/dashboard-layout"

interface EmployeeProfile {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  managerFirstName: string;
  managerLastName: string;
  jobTitle: string;
  department: string;
  employmentType: string;
  employmentStatus: string;
  startDate: string;
}

export default withAuth(function ProfilePage() {
  const [profile, setProfile] = useState<EmployeeProfile | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const response = await fetch('/api/employee/self', {
          credentials: 'include',
          headers: {
            'Accept': 'application/json'
          }
        });
        
        if (!response.ok) {
          const errorText = await response.text();
          console.error('Backend response:', errorText);
          throw new Error(`Failed to fetch profile: ${response.status}`);
        }
        
        const data = await response.json();
        setProfile(data);
      } catch (err) {
        console.error('Profile fetch error:', err);
        setError(err instanceof Error ? err.message : 'Failed to load profile');
      } finally {
        setLoading(false);
      }
    };

    fetchProfile();
  }, []);

  if (loading) {
    return <div>Loading...</div>;
  }

  if (error) {
    return <div>Error: {error}</div>;
  }

  return (
    <Suspense fallback={<div>Loading...</div>}>
      <DashboardLayout>
        <div className="flex-1 space-y-8 p-4 md:p-8 pt-6">
          {/* Header */}
          <div className="flex items-center justify-between">
            <div>
              <h2 className="text-3xl font-bold tracking-tight">Profil</h2>
              <p className="text-muted-foreground">Ihre persönlichen und beruflichen Informationen</p>
            </div>
          </div>

          <div className="grid gap-8">
            {/* Personal Information */}
            <Card className="border-2 border-black">
              <CardHeader>
                <div className="flex items-center space-x-4">
                  <Avatar className="h-20 w-20">
                    <AvatarImage src="/placeholder.svg" />
                    <AvatarFallback>{profile?.firstName?.[0]}{profile?.lastName?.[0]}</AvatarFallback>
                  </Avatar>
                  <div>
                    <CardTitle>Persönliche Informationen</CardTitle>
                    <CardDescription>Ihre grundlegenden Informationen</CardDescription>
                  </div>
                </div>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="grid gap-4 md:grid-cols-2">
                  <div>
                    <label className="text-sm font-medium">Vorname</label>
                    <Input value={profile?.firstName || ''} readOnly />
                  </div>
                  <div>
                    <label className="text-sm font-medium">Nachname</label>
                    <Input value={profile?.lastName || ''} readOnly />
                  </div>
                  <div>
                    <label className="text-sm font-medium">Email</label>
                    <Input value={profile?.email || ''} readOnly />
                  </div>
                  <div>
                    <label className="text-sm font-medium">Position</label>
                    <Input value={profile?.jobTitle || ''} readOnly />
                  </div>
                  <div>
                    <label className="text-sm font-medium">Abteilung</label>
                    <Input value={profile?.department || ''} readOnly />
                  </div>
                  <div>
                    <label className="text-sm font-medium">Beschäftigungsart</label>
                    <Input value={profile?.employmentType || ''} readOnly />
                  </div>
                  <div>
                    <label className="text-sm font-medium">Status</label>
                    <Input value={profile?.employmentStatus || ''} readOnly />
                  </div>
                  <div>
                    <label className="text-sm font-medium">Startdatum</label>
                    <Input value={profile?.startDate || ''} readOnly />
                  </div>
                </div>
                <div className="grid gap-4 md:grid-cols-2">
                  <div>
                    <label className="text-sm font-medium">Manager Vorname</label>
                    <Input value={profile?.managerFirstName || ''} readOnly />
                  </div>
                  <div>
                    <label className="text-sm font-medium">Manager Nachname</label>
                    <Input value={profile?.managerLastName || ''} readOnly />
                  </div>
                </div>
              </CardContent>
            </Card>
          </div>
        </div>
      </DashboardLayout>
    </Suspense>
  )
})